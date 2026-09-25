package main;

import java.util.Scanner;

import service.BookService;
import service.UserService;

public class Menu {

	public static void rentMenu(Scanner scan) {
		boolean bb = true;
		System.out.println("貸し出し及び返却をします。");

		while (bb) {

			System.out.println("番号を入力してください");
			System.out.println("1：貸し出し");
			System.out.println("2：返却");
			System.out.println("3：メインメニューに戻る");

			int sw = scan.nextInt();

			switch (sw) {
			case 1: {
				break;
			}

			case 2: {
				break;
			}

			case 3: {
				bb = false;
				System.out.println("メインメニューに戻ります");
				break;
			}

			default:
				System.out.println("不正な値です入力し直してください");
			}
		}
	}

	public static void dataEdit(Scanner scan) {
		boolean bb = true;
		System.out.println("データを管理します");

		while (bb) {

			System.out.println("番号を入力してください");
			System.out.println("1：本の追加");
			System.out.println("2：本の詳細・変更");
			System.out.println("3：ユーザーの登録");
			System.out.println("4：ユーザー詳細・変更");
			System.out.println("5：メインメニューへ戻る");

			int sw = scan.nextInt();

			switch (sw) {
			case 1: {
				BookService.addBook(scan);
				break;
			}

			case 2: {
				BookService.editBook(scan);
				break;
			}

			case 3: {
				UserService.addUser(scan);
				break;
			}

			case 4: {
				UserService.editUser(scan);
				break;
			}

			case 5: {
				bb = false;
				System.out.println("メインメニューに戻ります");
				break;
			}

			default:
				System.out.println("不正な値です入力し直してください");
			}
		}

	}
}
