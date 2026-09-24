package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class BookService {

	private static final Path dir = Path.of("BookData");

	public static void addBook(Scanner scan) {
		System.out.println("タイトルを入力してください");
		String title = scan.next();

		System.out.println("著者を入力してください");
		String author = scan.next();

		try {
			Files.createDirectories(dir);
			int bookId = nextBookId();
			boolean lent = false;

			Path file = dir.resolve(bookId + ".text");
			String body = title + System.lineSeparator()
					+ author + System.lineSeparator()
					+ lent + System.lineSeparator();

			Files.writeString(
					file,
					body,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE_NEW);

			System.out.println("登録しました。管理番号=" + bookId);
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}

	public static void editBook(Scanner scan) {
		showBooks();
		int bookId = readBookId(scan);

		if (!exists(bookId)) {
			return;
		}

		boolean bb = true;

		while (bb) {
			showOne(bookId);
			System.out.println("番号を入力してください");
			System.out.println("1：タイトルと著者の変更");
			System.out.println("2：削除");
			System.out.println("3：戻る");
			String choice = scan.next();

			switch (choice) {
			case "1": {
				changeBook(scan, bookId);
				break;
			}

			case "2": {
				System.out.println("削除する場合は 1 を入力してください");

				if ("1".equals(scan.next()) && deleteBook(bookId)) {
					bb = false;
				}

				break;
			}

			case "3": {
				bb = false;
				break;
			}

			default:
				System.out.println("不正な値です入力し直してください");
			}
		}
	}

	public static void showBooks() {
		try {
			if (Files.notExists(dir)) {
				System.out.println("本がありません。");
				return;
			}

			List<Path> files = new ArrayList<>();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.text")) {
				for (Path path : stream) {
					files.add(path);
				}

			}
			if (files.isEmpty()) {
				System.out.println("本がありません。");
				return;
			}

			files.sort(Comparator.comparingInt(BookService::bookIdFrom));

			for (Path path : files) {
				List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

				int bookId = bookIdFrom(path);

				if (lines.size() < 3) {
					System.out.println(bookId + ": ファイルの内容が不正です。");
					continue;
				}

				System.out.println(bookId + " | " + lines.get(0) + " | " + lines.get(1) + " | " + status(lines.get(2)));
			}
		} catch (IOException e) {
			System.out.println("一覧の取得に失敗しました。");

		} catch (NumberFormatException e) {
			System.out.println("管理番号ではないファイルがあります。");
		}
	}

	private static void showOne(int bookId) {
		try {
			List<String> lines = readLines(bookId);

			if (lines == null) {
				return;
			}

			System.out.println("管理番号: " + bookId);
			System.out.println("タイトル: " + lines.get(0));
			System.out.println("著者: " + lines.get(1));
			System.out.println("状態: " + status(lines.get(2)));
		} catch (IOException e) {
			System.out.println("読み込みに失敗しました。");
		}
	}

	private static void changeBook(Scanner scan, int bookId) {
		System.out.println("新しいタイトルを入力してください");
		String title = scan.next();

		System.out.println("新しい著者を入力してください");
		String author = scan.next();

		try {
			List<String> lines = readLines(bookId);
			if (lines == null) {
				return;
			}

			boolean lent = Boolean.parseBoolean(lines.get(2));
			writeBook(bookId, title, author, lent);
			System.out.println("変更しました。");
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}

	private static boolean deleteBook(int bookId) {
		try {
			Files.deleteIfExists(file(bookId));
			System.out.println("削除しました。");
			return true;
		} catch (IOException e) {
			System.out.println("削除に失敗しました。");
			return false;
		}
	}

	private static int readBookId(Scanner scan) {
		System.out.println("管理番号を入力してください");

		try {
			return Integer.parseInt(scan.next());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static boolean exists(int bookId) {
		if (bookId < 0) {
			System.out.println("管理番号は数値で入力してください。");
			return false;
		}

		if (Files.notExists(file(bookId))) {
			System.out.println("その管理番号の本はありません。");
			return false;
		}
		return true;
	}

	private static List<String> readLines(int bookId) throws IOException {
		List<String> lines = Files.readAllLines(file(bookId), StandardCharsets.UTF_8);

		if (lines.size() < 3) {
			System.out.println("ファイルの内容が不正です。");
			return null;
		}
		return lines;
	}

	private static void writeBook(int bookId, String title, String author, boolean lent) throws IOException {
		String body = title + System.lineSeparator()
				+ author + System.lineSeparator()
				+ lent + System.lineSeparator();

		Files.writeString(
				file(bookId),
				body,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	private static int nextBookId() throws IOException {
		int max = 0;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.text")) {
			for (Path path : stream) {
				String name = path.getFileName().toString();
				String number = name.substring(0, name.length() - ".text".length());
				max = Math.max(max, Integer.parseInt(number));
			}
		}
		return max + 1;
	}

	private static Path file(int bookId) {
		return dir.resolve(bookId + ".text");
	}

	private static int bookIdFrom(Path path) {
		String name = path.getFileName().toString();
		return Integer.parseInt(name.substring(0, name.length() - ".text".length()));
	}

	private static String status(String lentText) {
		return Boolean.parseBoolean(lentText) ? "貸出中" : "未貸出";
	}
}