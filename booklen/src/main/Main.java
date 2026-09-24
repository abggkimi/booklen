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
			System.out.println("1：貸し出し・返却処理");
			System.out.println("2：データの管理");
			System.out.println("3：終了");

			int sw = scan.nextInt();

			switch (sw) {
			case 1: {
				Menu.rentMenu(scan);
				break;
			}

			case 2: {
				Menu.dataEdit(scan);
				break;
			}

			case 3: {
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
