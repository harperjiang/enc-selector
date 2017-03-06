package edu.uchicago.cs.encsel.ndnn;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slow {

	static Logger log = LoggerFactory.getLogger(Slow.class);

	public static void main(String[] args) throws Exception {
		fast();
		slow();
		updated();
	}

	static void fast() {
		int hiddenDim = 200;
		int numChar = 100;
		int length = 500;
		int batchSize = 50;

		INDArray c2v = Nd4j.zeros(numChar, hiddenDim);

		INDArray h0 = Nd4j.zeros(batchSize, hiddenDim);
		INDArray c0 = Nd4j.zeros(batchSize, hiddenDim);

		INDArray fwdmap = Nd4j.zeros(batchSize, numChar);

		INDArray embed = fwdmap.mmul(c2v);

		List<INDArray> embeds = new ArrayList<>();
		List<INDArray> h0s = new ArrayList<>();
		for (int x = 0; x < 1000; x++) {
			embeds.add(Nd4j.createUninitialized(embed.shape()));
			h0s.add(Nd4j.createUninitialized(h0.shape()));
		}

		long sum = 0;

		for (int x = 0; x < embeds.size(); x++) {
			long time1 = System.nanoTime();
			INDArray concat = Nd4j.concat(1, embeds.get(x), h0s.get(x));
			long time2 = System.nanoTime();

			sum += time2 - time1;
		}
		System.out.println(sum / embeds.size());
	}

	static void slow() {
		int hiddenDim = 200;
		int numChar = 100;
		int length = 500;
		int batchSize = 50;

		INDArray c2v = Nd4j.zeros(numChar, hiddenDim);

		INDArray h0 = Nd4j.zeros(batchSize, hiddenDim);
		INDArray c0 = Nd4j.zeros(batchSize, hiddenDim);

		INDArray fwdmap = Nd4j.zeros(batchSize, numChar);

		INDArray embed = fwdmap.mmul(c2v);

		List<INDArray> embeds = new ArrayList<>();
		List<INDArray> h0s = new ArrayList<>();
		for (int x = 0; x < 1000; x++) {
			embeds.add(Nd4j.createUninitialized(embed.shape()));
			h0s.add(Nd4j.createUninitialized(h0.shape()));
		}

		long sum = 0;

		for (int x = 0; x < embeds.size(); x++) {
			embed = fwdmap.mmul(c2v);
			long time1 = System.nanoTime();
			INDArray concat = Nd4j.concat(1, embeds.get(x), h0s.get(x));
			long time2 = System.nanoTime();

			sum += time2 - time1;
		}
		System.out.println(sum / embeds.size());
	}

	static void updated() throws Exception {
		int hiddenDim = 200;
		int numChar = 100;
		int length = 500;
		int batchSize = 50;

		INDArray c2v = Nd4j.zeros(numChar, hiddenDim, 'f');

		INDArray h0 = Nd4j.zeros(batchSize, hiddenDim, 'f');
		INDArray c0 = Nd4j.zeros(batchSize, hiddenDim);

		INDArray fwdmap = Nd4j.zeros(batchSize, numChar);

		INDArray embed = fwdmap.mmul(c2v);

		List<INDArray> embeds = new ArrayList<>();
		List<INDArray> h0s = new ArrayList<>();
		List<INDArray> fwdmaps = new ArrayList<>();
		List<INDArray> c2vs = new ArrayList<>();
		for (int x = 0; x < 10000; x++) {
			embeds.add(Nd4j.createUninitialized(embed.shape(), embed.ordering()));
			h0s.add(Nd4j.createUninitialized(h0.shape(), h0.ordering()));
			c2vs.add(Nd4j.createUninitialized(c2v.shape(), c2v.ordering()));
			fwdmaps.add(Nd4j.createUninitialized(fwdmap.shape(), fwdmap.ordering()));
		}

		log.info("GEMM tests:");

		for (int x = 0; x < embeds.size(); x++) {
			long time1 = System.nanoTime();
			fwdmaps.get(x).mmul(c2vs.get(x));
			long time2 = System.nanoTime();

			if (x % 100 == 0)
				log.info("Concat time: {} us", (time2 - time1) / 1000);
		}

		log.info("Concat tests:");

		for (int x = 0; x < embeds.size(); x++) {
			embed = fwdmaps.get(x).mmul(c2vs.get(x));
			long time1 = System.nanoTime();
			INDArray concat = Nd4j.concat(1, embeds.get(x), h0s.get(x));
			long time2 = System.nanoTime();

			if (x % 100 == 0)
				log.info("Concat time: {} us", (time2 - time1) / 1000);
		}

	}

	static INDArray xavier(int[] shape) {
		int n = 1;
		for (int i = 0; i < shape.length - 1; i++)
			n *= shape[i];
		double sd = Math.sqrt(3d / n);
		return new UniformDistribution(-sd, sd).sample(shape);
	}
}
