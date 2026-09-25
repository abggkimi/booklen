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

public class UserService {

	private static final Path dir = Path.of("UserData");

	public static void addUser(Scanner scan) {
		System.out.println("利用者名を入力してください");
		System.out.println("※スペースを入力しないでください");
		String name = scan.next();
		String rest = scan.nextLine();

		if (!rest.isBlank()) {
			System.out.println("スペースは使用できません。");
			return;
		}

		try {
			Files.createDirectories(dir);
			int userId = nextUserId();
			Path file = dir.resolve(userId + ".text");
			String body = name + System.lineSeparator();

			Files.writeString(
					file,
					body,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE_NEW);

			System.out.println("登録しました。利用者番号は" + userId + "です");
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}

	public static void editUser(Scanner scan) {
		showUsers();
		int userId = readUserId(scan);
		if (!exists(userId)) {
			return;
		}

		boolean bb = true;
		while (bb) {
			showOne(userId);
			System.out.println("番号を入力してください");
			System.out.println("1：利用者名の変更");
			System.out.println("2：削除");
			System.out.println("3：戻る");
			String choice = scan.next();

			switch (choice) {
			case "1": {
				changeUser(scan, userId);
				break;
			}

			case "2": {
				System.out.println("削除する場合は 1 を入力してください");
				if ("1".equals(scan.next()) && deleteUser(userId)) {
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

	public static void showUsers() {
		try {
			if (Files.notExists(dir)) {
				System.out.println("利用者がありません。");
				return;
			}
			List<Path> files = new ArrayList<>();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.text")) {
				for (Path path : stream) {
					files.add(path);
				}
			}
			if (files.isEmpty()) {
				System.out.println("利用者がありません。");
				return;
			}
			files.sort(Comparator.comparingInt(UserService::userIdFrom));
			for (Path path : files) {
				List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
				int userId = userIdFrom(path);

				if (lines.isEmpty()) {
					System.out.println(userId + ": ファイルの内容が不正です。");
					continue;
				}
				System.out.println(userId + " | " + lines.get(0));
			}
		} catch (IOException e) {
			System.out.println("一覧の取得に失敗しました。");
		} catch (NumberFormatException e) {
			System.out.println("利用者番号ではないファイルがあります。");
		}
	}

	private static void showOne(int userId) {
		try {
			List<String> lines = readLines(userId);

			if (lines == null) {
				return;
			}
			System.out.println("利用者番号: " + userId);
			System.out.println("利用者名: " + lines.get(0));
			if (lines.size() == 1) {
				System.out.println("貸し出し履歴はありません。");
				return;
			}

			System.out.println("貸し出し履歴:");
			for (int i = 1; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.isBlank()) {
					continue;
				}
				String[] parts = line.split("\\|", -1);

				if (parts.length < 3) {
					System.out.println("履歴の内容が不正です。");
					continue;
				}
				System.out.println("貸出日: " + parts[0]);
				String returned = "-".equals(parts[1]) ? "未返却" : parts[1];
				System.out.println("返却日: " + returned);
				System.out.println("bookId: " + parts[2]);
			}
		} catch (IOException e) {
			System.out.println("読み込みに失敗しました。");
		}
	}

	private static void changeUser(Scanner scan, int userId) {
		System.out.println("新しい利用者名を入力してください");
		System.out.println("※スペースを入力しないでください");
		String name = scan.next();
		String rest = scan.nextLine();

		if (!rest.isBlank()) {
			System.out.println("スペースは使用できません。");
			return;
		}

		try {
			List<String> lines = readLines(userId);
			if (lines == null) {
				return;
			}
			lines.set(0, name);
			writeUser(userId, lines);
			System.out.println("変更しました。");
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}

	private static boolean deleteUser(int userId) {
		try {
			Files.deleteIfExists(file(userId));
			System.out.println("削除しました。");
			return true;
		} catch (IOException e) {
			System.out.println("削除に失敗しました。");
			return false;
		}
	}

	private static int readUserId(Scanner scan) {
		System.out.println("利用者番号を入力してください");
		try {
			return Integer.parseInt(scan.next());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static boolean exists(int userId) {
		if (userId < 0) {
			System.out.println("利用者番号は数値で入力してください。");
			return false;
		}
		if (Files.notExists(file(userId))) {
			System.out.println("その利用者番号はありません。");
			return false;
		}
		return true;
	}

	private static List<String> readLines(int userId) throws IOException {
		List<String> lines = Files.readAllLines(file(userId), StandardCharsets.UTF_8);

		if (lines.isEmpty()) {
			System.out.println("ファイルの内容が不正です。");
			return null;
		}
		return lines;
	}

	private static void writeUser(int userId, List<String> lines) throws IOException {
		String body = String.join(System.lineSeparator(), lines) + System.lineSeparator();
		Files.writeString(
				file(userId),
				body,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	private static int nextUserId() throws IOException {
		int max = 0;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.text")) {
			for (Path path : stream) {
				String fileName = path.getFileName().toString();
				String number = fileName.substring(0, fileName.length() - ".text".length());
				max = Math.max(max, Integer.parseInt(number));
			}
		}
		return max + 1;
	}

	private static Path file(int userId) {
		return dir.resolve(userId + ".text");
	}

	private static int userIdFrom(Path path) {
		String fileName = path.getFileName().toString();
		return Integer.parseInt(fileName.substring(0, fileName.length() - ".text".length()));
	}
}