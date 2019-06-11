package com.satech.pharmacy.connector;

public class StringUtils {

	/*
	 * This function checks the validity of the desired scanner output and parse
	 * it. It returns string array, whose first index is scanner_id, second
	 * index is box_number if parameter's format is not valid, it returns null
	 * array
	 */
	public static String[] checkNparseBarcode(String barcode_out) throws Exception {
		String[] array1 = new String[2];
		int length = barcode_out.length();

		if ((length == 8) && (barcode_out.charAt(0) == 'e') && (barcode_out.charAt(7) == 'i')) {
			array1[0] = "1";
			array1[1] = barcode_out.substring(1, 7);
		} else if ((length == 8) && (barcode_out.charAt(0) == 'i') && (barcode_out.charAt(7) == 'e')) {
			array1[0] = "2";
			array1[1] = barcode_out.substring(1, 7);
		} else if ((length == 8) && (barcode_out.charAt(0) == 'c') && (barcode_out.charAt(7) == 'm')) {
			array1[0] = "3";
			array1[1] = barcode_out.substring(1, 7);
		}
		else {
			array1[0] = null;
			array1[1] = null;
		}
		return array1;
	}
}
