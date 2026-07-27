package org.owasp.untrust.tesseractmock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TesseractMock {
    private static final List<String> OUTPUTS = List.of(
            """
            Buy printer paper

            Order two boxes of A4 printer paper for the office supply cabinet.
            """,
            """
            Renew passport

            Fill the renewal form, attach a photo, and schedule the appointment.
            """,
            """
            Pay utility bill

            Electricity bill is due this Friday. Confirm payment receipt afterward.
            """,
            """
            Call project vendor

            Ask for the revised delivery estimate and request written confirmation.
            """,
            """
            Prepare release notes

            Summarize fixed bugs, migration notes, and known limitations for review.
            """
    );

    private TesseractMock() {
    }

    public static void main(String[] args) throws Exception {
        Arguments parsed = Arguments.parse(args);
        byte[] imageBytes = Files.readAllBytes(parsed.input());
        int outputIndex = Math.floorMod(imageBytes.length + parsed.language().hashCode() + parsed.pageSegmentationMode(), OUTPUTS.size());
        System.out.print(OUTPUTS.get(outputIndex));
    }

    private record Arguments(Path input, String outputBase, String language, int pageSegmentationMode) {
        private static Arguments parse(String[] args) {
            if (args.length != 6) {
                usage();
            }
            if (!"stdout".equals(args[1])) {
                usage();
            }
            if (!"-l".equals(args[2])) {
                usage();
            }
            if (args[3].isBlank()) {
                usage();
            }
            if (!"--psm".equals(args[4])) {
                usage();
            }
            return new Arguments(Path.of(args[0]), args[1], args[3], Integer.parseInt(args[5]));
        }

        private static void usage() {
            System.err.println("usage: tesseract <input-file> stdout -l <language> --psm <mode>");
            System.exit(2);
        }
    }
}
