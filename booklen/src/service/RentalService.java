package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class RentalService {

	private static final Path bookDir = Path.of("BookData");
	private static final Path userDir = Path.of("UserData");

	public static void rental(Scanner scan) {
		UserService.showUsers();
		int userId = readId(scan, "利用者番号を入力してください");

		if (userId < 0) {
			System.out.println("利用者番号は数値で入力してください。");
			return;
		}
		Path userFile = userDir.resolve(userId + ".text");

		if (Files.notExists(userFile)) {
			System.out.println("その利用者番号はありません。");
			return;
		}

		BookService.showBooks();
		int bookId = readId(scan, "管理番号を入力してください");

		if (bookId < 0) {
			System.out.println("管理番号は数値で入力してください。");
			return;
		}
		Path bookFile = bookDir.resolve(bookId + ".text");

		if (Files.notExists(bookFile)) {
			System.out.println("その管理番号の本はありません。");
			return;
		}

		try {
			List<String> userLines = Files.readAllLines(userFile, StandardCharsets.UTF_8);
			List<String> bookLines = Files.readAllLines(bookFile, StandardCharsets.UTF_8);

			if (userLines.isEmpty() || bookLines.size() < 3) {
				System.out.println("ファイルの内容が不正です。");
				return;
			}

			if (Boolean.parseBoolean(bookLines.get(2))) {
				System.out.println("すでに貸出中です。");
				return;
			}

			String loanDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy/MM/dd/HH/mm"));
			userLines.add(loanDate + "|-|" + bookId);
			String userBody = String.join(System.lineSeparator(), userLines) + System.lineSeparator();
			Files.writeString(
					userFile,
					userBody,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);

			String bookBody = bookLines.get(0) + System.lineSeparator()
					+ bookLines.get(1) + System.lineSeparator()
					+ true + System.lineSeparator();
			Files.writeString(
					bookFile,
					bookBody,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);

			System.out.println("貸し出しました。");
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}

	public static void returnBook(Scanner scan) {
		UserService.showUsers();
		int userId = readId(scan, "利用者番号を入力してください");

		if (userId < 0) {
			System.out.println("利用者番号は数値で入力してください。");
			return;
		}

		Path userFile = userDir.resolve(userId + ".text");
		if (Files.notExists(userFile)) {
			System.out.println("その利用者番号はありません。");
			return;
		}

		try {
			List<String> userLines = Files.readAllLines(userFile, StandardCharsets.UTF_8);
			if (userLines.isEmpty()) {
				System.out.println("ファイルの内容が不正です。");
				return;
			}
			if (!showLending(userLines)) {
				System.out.println("貸し出し中の本はありません。");
				return;
			}

			int bookId = readId(scan, "管理番号を入力してください");
			if (bookId < 0) {
				System.out.println("管理番号は数値で入力してください。");
				return;
			}

			int target = -1;

			for (int i = 1; i < userLines.size(); i++) {
				String line = userLines.get(i);
				if (line.isBlank()) {
					continue;
				}
				String[] parts = line.split("\\|", -1);
				if (parts.length >= 3 && String.valueOf(bookId).equals(parts[2]) && "-".equals(parts[1])) {
					target = i;
				}
			}
			if (target < 0) {
				System.out.println("この利用者の貸出中の本ではありません。");
				return;
			}

			Path bookFile = bookDir.resolve(bookId + ".text");

			if (Files.notExists(bookFile)) {
				System.out.println("その管理番号の本はありません。");
				return;
			}
			List<String> bookLines = Files.readAllLines(bookFile, StandardCharsets.UTF_8);

			if (bookLines.size() < 3) {
				System.out.println("ファイルの内容が不正です。");
				return;
			}

			String[] parts = userLines.get(target).split("\\|", -1);
			String returnDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yy/MM/dd"));
			userLines.set(target, parts[0] + "|" + returnDate + "|" + parts[2]);
			String userBody = String.join(System.lineSeparator(), userLines) + System.lineSeparator();
			Files.writeString(
					userFile,
					userBody,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);

			String bookBody = bookLines.get(0) + System.lineSeparator()
					+ bookLines.get(1) + System.lineSeparator()
					+ false + System.lineSeparator();

			Files.writeString(
					bookFile,
					bookBody,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);

			System.out.println("返却しました。");
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}

	private static boolean showLending(List<String> userLines) {
		boolean found = false;

		for (int i = 1; i < userLines.size(); i++) {
			String line = userLines.get(i);

			if (line.isBlank()) {
				continue;
			}
			String[] parts = line.split("\\|", -1);

			if (parts.length < 3 || !"-".equals(parts[1])) {
				continue;
			}
			String title = parts[2];
			Path bookFile = bookDir.resolve(parts[2] + ".text");

			try {
				if (Files.exists(bookFile)) {
					List<String> bookLines = Files.readAllLines(bookFile, StandardCharsets.UTF_8);
					if (!bookLines.isEmpty()) {
						title = bookLines.get(0);
					}
				}
			} catch (IOException e) {
				title = parts[2];
			}
			System.out.println(parts[2] + " | " + title + " | 未返却");
			found = true;
		}
		return found;
	}

	private static int readId(Scanner scan, String message) {
		System.out.println(message);
		try {
			return Integer.parseInt(scan.next());
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
