package service;

import java.util.Scanner;

public class UserService {
	public static void rent(Scanner scan) {
		boolean b = true;
		System.out.println("レンタル処理をします");

		System.out.println("利用者番号を入力してください");
		int userId = scan.nextInt();

		System.out.println("貸し出しする書籍の書籍番号を入力してください");
		int bookId = scan.nextInt();

	}
}
