package org.example.nes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpCodeEnumGenerator {
    private static final String SCRAPE_URL = "https://www.masswerk.at/6502/6502_instruction_set.html";

    private static final Map<String, AddressMode> ADDRESS_MODE_MAP = Map.ofEntries(
            Map.entry("impl", AddressMode.IMPLIED),
            Map.entry("rel", AddressMode.RELATIVE),
            Map.entry("abs", AddressMode.ABSOLUTE),
            Map.entry("#", AddressMode.IMMEDIATE),
            Map.entry("X,ind", AddressMode.X_INDEXED_INDIRECT),
            Map.entry("ind,Y", AddressMode.INDIRECT_Y_INDEXED),
            Map.entry("zpg", AddressMode.ZEROPAGE),
            Map.entry("zpg,X", AddressMode.ZEROPAGE_X_INDEXED),
            Map.entry("zpg,Y", AddressMode.ZEROPAGE_Y_INDEXED),
            Map.entry("abs,Y", AddressMode.ABSOLUTE_Y_INDEXED),
            Map.entry("A", AddressMode.ACCUMULATOR),
            Map.entry("abs,X", AddressMode.ABSOLUTE_X_INDEXED),
            Map.entry("ind", AddressMode.INDIRECT)
    );

    public static void main(String[] args) throws IOException {
        final List<TableEntry> entries = fetchTableEntries();
        final String enumEntries = generateEnumClasEntries(entries);
        System.out.println(enumEntries);
    }

    private static List<TableEntry> fetchTableEntries() throws IOException {
        final List<TableEntry> tableEntries = new ArrayList<>(0x100);
        final Document doc = Jsoup.connect(SCRAPE_URL).get();
        final Elements rows = doc.select("#opctable tbody tr");
        for (int i = 0; i < 0x10; i++) {
            final Elements cols = rows.get(i).select("td");
            for (int j = 0; j < 0x10; j++) {
                final String text = cols.get(j).text();
                if (text.equals("---")) {
                    continue;
                }
                final String[] split = text.split(" ", 2);
                final int opCode = i << 4 | j;
                final Operation operation = Operation.valueOf(split[0]);
                final AddressMode addressMode = ADDRESS_MODE_MAP.get(split[1]);
                tableEntries.add(new TableEntry(opCode, operation, addressMode));
            }
        }
        return tableEntries;
    }

    private static String generateEnumClasEntries(List<TableEntry> tableEntries) {
        final StringBuilder stringBuilder = new StringBuilder(tableEntries.size() * 35);
        for (TableEntry entry : tableEntries) {
            stringBuilder.append(entry.operation.name());
            stringBuilder.append('_');
            stringBuilder.append(entry.addressMode.shortName);
            stringBuilder.append("((byte) 0x");
            if (entry.opCode < 0x10) {
                stringBuilder.append('0');
            }
            stringBuilder.append(Integer.toUnsignedString(entry.opCode, 16));
            stringBuilder.append(", ");
            stringBuilder.append(entry.operation.name());
            stringBuilder.append(", ");
            stringBuilder.append(entry.addressMode.name());
            stringBuilder.append("),\n");
        }
        return stringBuilder.toString();
    }

    private record TableEntry(int opCode, Operation operation, AddressMode addressMode) {
    }
}
