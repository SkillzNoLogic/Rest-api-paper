package me.skillznologic.reststatsapi.core;

import java.util.Collection;
import java.util.Map;

/** Tiny dependency-free JSON writer (keeps the jar portable across every platform and Gson version). */
public final class Json {

    private Json() {
    }

    public static String players(Collection<PlayerStats> players) {
        StringBuilder sb = new StringBuilder(64 + players.size() * 128);
        sb.append('[');
        boolean first = true;
        for (PlayerStats p : players) {
            if (!first) sb.append(',');
            first = false;
            sb.append("{\"uuid\":");
            quote(sb, p.getUuid());
            if (p.getName() != null) {
                sb.append(",\"name\":");
                quote(sb, p.getName());
            }
            sb.append(",\"stats\":{");
            boolean firstStat = true;
            for (Map.Entry<String, String> e : p.getStats().entrySet()) {
                if (!firstStat) sb.append(',');
                firstStat = false;
                quote(sb, e.getKey());
                sb.append(':');
                if (e.getValue() == null) sb.append("null");
                else quote(sb, e.getValue());
            }
            sb.append("}}");
        }
        return sb.append(']').toString();
    }

    public static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        quote(sb, s);
        return sb.toString();
    }

    private static void quote(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }
}
