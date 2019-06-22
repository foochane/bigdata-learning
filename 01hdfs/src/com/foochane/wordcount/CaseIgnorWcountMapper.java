package com.foochane.wordcount;

public class CaseIgnorWcountMapper implements Mapper {

	@Override
	public void map(String line, Context context) {

		String[] words = line.toUpperCase().split(" ");

		for (String word : words) {

			Object value = context.get(word);
			if (null == value) {
				context.write(word, 1);

			} else {
				int v = (int) value;
				context.write(word, v + 1);
			}

		}

	}

}
