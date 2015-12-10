import java.util.*;

public class Main {

	public static void main(String[] args) {
		Starter test = new Starter();

		// if the program is not given a filepath argument it accepts command
		// line input, instruction by instruction else it creates a filestream
		// in the starter class to parse the file.
		if (args.length == 0) {
			test.run();
		} else {
			String fileName = args[0];
			test.UseFile(fileName);
			test.run();
		}

	}
}
