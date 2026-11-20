package com.postmanlite.service;

/** Formateador JSON minimo (sin dependencias). Si el texto no parece JSON lo devuelve igual. */
public final class JsonPretty {

    private JsonPretty() {}

    public static String format(String text) {
        String s = text == null ? "" : text.trim();
        if (!(s.startsWith("{") || s.startsWith("["))) {
            return text == null ? "" : text;
        }
        StringBuilder out = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                out.append(c);
                if (c == '\\' && i + 1 < s.length()) {
                    out.append(s.charAt(++i));
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            switch (c) {
                case '"' -> { inString = true; out.append(c); }
                case '{', '[' -> {
                    int next = nextNonSpace(s, i + 1);
                    if (next < s.length() && (s.charAt(next) == '}' || s.charAt(next) == ']')) {
                        out.append(c).append(s.charAt(next));   // contenedor vacio: {} o []
                        i = next;
                    } else {
                        out.append(c).append('\n');
                        indent++;
                        pad(out, indent);
                    }
                }
                case '}', ']' -> {
                    indent = Math.max(0, indent - 1);
                    out.append('\n');
                    pad(out, indent);
                    out.append(c);
                }
                case ',' -> { out.append(c).append('\n'); pad(out, indent); }
                case ':' -> out.append(": ");
                case ' ', '\n', '\r', '\t' -> { /* se descarta el espacio original */ }
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static int nextNonSpace(String s, int from) {
        int i = from;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    private static void pad(StringBuilder sb, int level) {
        sb.append("  ".repeat(level));
    }
}
