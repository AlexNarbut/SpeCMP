import java.util.prefs.Preferences;

/**
 * Данные desktop-приложения в java.util.prefs userRoot (на macOS — общий для всех JVM-приложений plist).
 * Через Java API, а не правкой plist: cfprefsd кэширует файл.
 *
 * java Prefs.java dump <prefix>...    — «node/key<TAB>value» узлов корня и ключей корня с префиксом
 * java Prefs.java remove <prefix>...  — удалить такие узлы и ключи
 */
public class Prefs {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Prefs.java dump|remove <prefix>...");
            System.exit(2);
        }
        Preferences root = Preferences.userRoot();
        boolean remove = args[0].equals("remove");
        for (String child : root.childrenNames()) {
            if (!matches(child, args)) continue;
            Preferences node = root.node(child);
            if (remove) {
                node.removeNode();
                System.out.println("removed " + child + "/");
            } else {
                for (String key : node.keys()) System.out.println(child + "/" + key + "\t" + node.get(key, ""));
            }
        }
        for (String key : root.keys()) {
            if (!matches(key, args)) continue;
            if (remove) {
                root.remove(key);
                System.out.println("removed " + key);
            } else {
                System.out.println(key + "\t" + root.get(key, ""));
            }
        }
        root.flush();
    }

    private static boolean matches(String name, String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (name.startsWith(args[i])) return true;
        }
        return false;
    }
}
