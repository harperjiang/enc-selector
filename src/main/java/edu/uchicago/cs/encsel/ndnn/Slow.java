package edu.uchicago.cs.encsel.ndnn;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Slow {

	public static void main(String[] args) {
		int hiddenDim = 200;
		int numChar = 100;
		int length = 500;
		int batchSize = 50;

		// INDArray c2v = xavier(new int[] { numChar, hiddenDim });
		INDArray c2v = Nd4j.zeros(numChar, hiddenDim);
		INDArray h0 = Nd4j.zeros(batchSize, hiddenDim);
		INDArray c0 = Nd4j.zeros(batchSize, hiddenDim);

		INDArray fwdmap = Nd4j.zeros(batchSize, numChar);

		INDArray embed = fwdmap.mmul(c2v);

		List<INDArray> embeds = new ArrayList<>();
		List<INDArray> h0s = new ArrayList<>();
		for (int x = 0; x < 10000; x++) {
			embeds.add(Nd4j.createUninitialized(embed.shape()));
			h0s.add(Nd4j.createUninitialized(h0.shape()));
		}

		for (int x = 0; x < embeds.size(); x++) {
			long time1 = System.nanoTime();
			INDArray concat = Nd4j.concat(1, embeds.get(x), h0s.get(x));
			long time2 = System.nanoTime();

			if (x % 10 == 0)
				System.out.println("Concat time: " + ((time2 - time1) / 1000) + " us");
		}
	}
}
