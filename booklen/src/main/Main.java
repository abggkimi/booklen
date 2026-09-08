package main;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		Scanner scan = new Scanner(System.in);
		boolean b = true;

		System.out.println("booklenへようこそ！");

		while (b) {

			System.out.println("番号を入力してください");
			System.out.println("1：");

			int sw = scan.nextInt();

			switch (sw) {
			case 1: {
				b = false;
				System.out.println("終了します...");
				break;
			}
			default:
				System.out.println("不正な値です入力し直してください");
			}
		}
		scan.close();
	}

}
