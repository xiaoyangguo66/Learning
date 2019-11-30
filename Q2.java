import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

class Coordinates implements Writable {
	private double x;
	private double y;

	public Coordinates() {

	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeDouble(x);
		dataOutput.writeDouble(y);
	}

	@Override
	public void readFields(DataInput dataInput) throws IOException {
		x = dataInput.readDouble();
		y = dataInput.readDouble();
	}

	@Override
	public String toString() {
		return x + " " + y;
	}
}
public class Q2{
	static class Q2Mapper extends Mapper<Object, Text, Text, Coordinates> {

		@Override
		protected void map (Object key, Text value, Mapper<Object, Text, Text, Coordinates>.Context context)
				throws IOException, InterruptedException {
			String[] tokens = value.toString().split(" ");
			if (tokens.length > 1) {
				Text name = new Text();
				name.set(tokens[0]);
				double x = Double.parseDouble(tokens[1]);
				double y = Double.parseDouble(tokens[2]);
				Coordinates coordinate = new Coordinates(x, y);
				context.write(name, coordinate);
			}
		}
	}

	static class Q2Reducer extends Reducer<Text, Coordinates, Text, DoubleWritable> {
		@Override
		protected void reduce(Text key, Iterable<Coordinates> values,
		                      Reducer<Text, Coordinates, Text,DoubleWritable>.Context context)
				throws IOException, InterruptedException {
			double maxDistance = 0;
			double distance = 0;
			List<Coordinates> list = new ArrayList<Coordinates>();
			for (Coordinates bean : values) {
				list.add(new Coordinates(bean.getX(), bean.getY()));
			}
			for (int i = 0; i < list.size(); i++) {
				for (int j = i + 1; j < list.size(); j++) {
					distance = sqrt(Math.pow((list.get(j).getX() - list.get(i).getX()), 2)
							+  Math.pow((list.get(j).getY() - list.get(i).getY()), 2));
					maxDistance = Math.max(distance, maxDistance);
				}
			}

			context.write(key, new DoubleWritable(maxDistance));
		}

	}

	public static void main(String[] args) throws Exception {
		org.apache.hadoop.conf.Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(Q2.class);
		job.setMapperClass(Q2Mapper.class);
		job.setReducerClass(Q2Reducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Coordinates.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
