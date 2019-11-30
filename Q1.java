import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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

class Coordinates implements Writable{
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

public class Q1{
	static class Q1Mapper extends Mapper<Object, Text, Text, Coordinates> {

		@Override
		protected void map (Object key, Text value, Mapper<Object, Text, Text, Coordinates>.Context context)
			throws  IOException, InterruptedException {

			String[] tokens = value.toString().split(" ");

			if (tokens.length > 1) {

				Text name = new Text();
				name.set(tokens[0]);
				//System.out.println(tokens[0] + " " + "length:" + tokens.length);
				double tempX = Double.parseDouble(tokens[1]);
				double tempY = Double.parseDouble(tokens[2]);

				Coordinates coordinates = new Coordinates(tempX, tempY);

				context.write(name, coordinates);
			}
		}
	}

	static class Q1Reducer extends Reducer<Text, Coordinates, Text, Coordinates> {

		@Override
		protected void reduce(Text key, Iterable<Coordinates> values,
		                      Reducer<Text, Coordinates, Text, Coordinates>.Context context)
			throws IOException, InterruptedException {
			double sum1 = 0;
			double sum2 = 0;
			int count = 0;
			double result1 = 0;
			double result2 = 0;
			for (Coordinates bean : values) {
				sum1 += bean.getX();
				sum2 += bean.getY();
				count ++;
			}
			result1 = sum1 / count;
			result2 = sum2 / count;
			Coordinates resultCoordinate = new Coordinates(result1, result2);
			context.write(key, resultCoordinate);
		}

	}

	public static void main(String[] args) throws Exception {
		org.apache.hadoop.conf.Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(Q1.class);
		job.setMapperClass(Q1Mapper.class);
		job.setReducerClass(Q1Reducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Coordinates.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Coordinates.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}