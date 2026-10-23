package com.gathin.tvhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Order, dock membership, and hidden apps for the home screen.
 *
 * The config dock is the default. A configured package that is not installed does not
 * leave a hole: the next apps fill the row. After the user pins, unpins, or moves a tile
 * into or out of that row, the saved dock replaces the config list. New packages append
 * after the saved order. Uninstalled packages are dropped. Config-hidden packages (stock
 * launchers) stay hidden across reset; packages the user hides come back on reset.
 */
public final class AppOrder {

    public static final class App {
        public final String pkg;
        public final String label;

        public App(String pkg, String label) {
            this.pkg = pkg;
            this.label = label == null ? "" : label;
        }
    }

    /** Persisted choices. {@code userDock} false means the config dock is still in charge. */
    public static final class State {
        public final List<String> order;
        public final List<String> dock;
        public final Set<String> hidden;
        public final boolean userDock;

        public State(List<String> order, List<String> dock, Set<String> hidden, boolean userDock) {
            this.order = Collections.unmodifiableList(copy(order));
            this.dock = Collections.unmodifiableList(copy(dock));
            LinkedHashSet<String> hide = new LinkedHashSet<>();
            if (hidden != null) hide.addAll(hidden);
            this.hidden = Collections.unmodifiableSet(hide);
            this.userDock = userDock;
        }

        public static State fresh() {
            return new State(Collections.emptyList(), Collections.emptyList(), Collections.emptySet(), false);
        }
    }

    /** What the grid shows. {@code pinned} is the explicit dock, a prefix of {@code packages}. */
    public static final class Layout {
        public final List<String> packages;
        public final int dockCount;
        public final List<String> pinned;

        Layout(List<String> packages, int dockCount, List<String> pinned) {
            this.packages = Collections.unmodifiableList(packages);
            this.dockCount = dockCount;
            this.pinned = Collections.unmodifiableList(pinned);
        }
    }

    private AppOrder() {
    }

    public static Layout layout(List<App> installed, List<String> configDock, Set<String> configHidden,
                                State state, int columns) {
        int cols = Math.max(1, columns);
        State saved = state == null ? State.fresh() : state;
        List<String> config = configDock == null ? Collections.emptyList() : configDock;
        Set<String> alwaysHidden = configHidden == null ? Collections.emptySet() : configHidden;

        List<App> visible = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (installed != null) {
            for (App app : installed) {
                if (app == null || app.pkg == null || app.pkg.isEmpty()) continue;
                if (!seen.add(app.pkg)) continue;
                if (alwaysHidden.contains(app.pkg) || saved.hidden.contains(app.pkg)) continue;
                visible.add(app);
            }
        }

        List<String> base = new ArrayList<>();
        if (saved.order.isEmpty()) {
            visible.sort((a, b) -> compareLabel(a, b));
            for (App app : visible) base.add(app.pkg);
        } else {
            Set<String> known = new LinkedHashSet<>();
            for (App app : visible) known.add(app.pkg);
            for (String pkg : saved.order) {
                if (known.contains(pkg)) base.add(pkg);
            }
            List<App> fresh = new ArrayList<>();
            for (App app : visible) {
                if (!base.contains(app.pkg)) fresh.add(app);
            }
            fresh.sort((a, b) -> compareLabel(a, b));
            for (App app : fresh) base.add(app.pkg);
        }

        List<String> pins = new ArrayList<>();
        Set<String> visiblePkgs = new LinkedHashSet<>(base);
        if (saved.userDock) {
            for (String pkg : saved.dock) {
                if (visiblePkgs.contains(pkg) && !pins.contains(pkg)) pins.add(pkg);
                if (pins.size() == cols) break;
            }
        } else {
            for (String pkg : config) {
                if (visiblePkgs.contains(pkg) && !pins.contains(pkg)) pins.add(pkg);
                if (pins.size() == cols) break;
            }
        }

        List<String> shown = new ArrayList<>(pins);
        for (String pkg : base) {
            if (!shown.contains(pkg)) shown.add(pkg);
        }
        int dockCount = Math.min(cols, shown.size());
        return new Layout(shown, dockCount, pins);
    }

    /** Drop packages that are no longer installed. Hidden names are kept so a reinstall stays hidden. */
    public static State prune(State state, Set<String> installedPackages) {
        State saved = state == null ? State.fresh() : state;
        Set<String> installed = installedPackages == null ? Collections.emptySet() : installedPackages;
        List<String> order = new ArrayList<>();
        for (String pkg : saved.order) {
            if (installed.contains(pkg)) order.add(pkg);
        }
        List<String> dock = new ArrayList<>();
        for (String pkg : saved.dock) {
            if (installed.contains(pkg)) dock.add(pkg);
        }
        if (order.equals(saved.order) && dock.equals(saved.dock)) return saved;
        return new State(order, dock, saved.hidden, saved.userDock);
    }

    /** Move one tile. A move that stays in the grid does not take the dock away from config. */
    public static State move(State state, Layout current, int from, int to) {
        State saved = state == null ? State.fresh() : state;
        if (current == null) return saved;
        List<String> shown = new ArrayList<>(current.packages);
        if (from < 0 || to < 0 || from >= shown.size() || to >= shown.size() || from == to) return saved;
        String moved = shown.remove(from);
        shown.add(to, moved);
        boolean touchesDock = from < current.dockCount || to < current.dockCount;
        if (!touchesDock) {
            return new State(shown, saved.dock, saved.hidden, saved.userDock);
        }
        int dockCount = Math.min(current.dockCount, shown.size());
        return new State(shown, new ArrayList<>(shown.subList(0, dockCount)), saved.hidden, true);
    }

    /** Pin replaces the config dock. The previous explicit pins are kept, up to one row. */
    public static State pin(State state, Layout current, String pkg, int columns) {
        State saved = state == null ? State.fresh() : state;
        if (current == null || pkg == null || !current.packages.contains(pkg)) return saved;
        int cols = Math.max(1, columns);
        List<String> pins = new ArrayList<>(current.pinned);
        pins.remove(pkg);
        if (pins.size() >= cols) pins.remove(pins.size() - 1);
        pins.add(pkg);
        return new State(orderWithPins(pins, current.packages), pins, saved.hidden, true);
    }

    /**
     * Take the app out of the explicit dock and park it on the row below the filled dock,
     * so the gap-fill does not pull it straight back.
     */
    public static State unpin(State state, Layout current, String pkg, int columns) {
        State saved = state == null ? State.fresh() : state;
        if (current == null || pkg == null || !current.packages.contains(pkg)) return saved;
        int cols = Math.max(1, columns);
        List<String> pins = new ArrayList<>(saved.userDock ? saved.dock : current.pinned);
        pins.remove(pkg);
        if (pins.size() > cols) pins = new ArrayList<>(pins.subList(0, cols));
        List<String> rest = new ArrayList<>();
        for (String other : current.packages) {
            if (!other.equals(pkg) && !pins.contains(other)) rest.add(other);
        }
        int fill = Math.max(0, Math.min(cols - pins.size(), rest.size()));
        List<String> order = new ArrayList<>(pins);
        order.addAll(rest.subList(0, fill));
        order.add(pkg);
        if (fill < rest.size()) order.addAll(rest.subList(fill, rest.size()));
        return new State(order, pins, saved.hidden, true);
    }

    public static State hide(State state, String pkg) {
        State saved = state == null ? State.fresh() : state;
        if (pkg == null || pkg.isEmpty() || saved.hidden.contains(pkg)) return saved;
        LinkedHashSet<String> hidden = new LinkedHashSet<>(saved.hidden);
        hidden.add(pkg);
        return new State(without(saved.order, pkg), without(saved.dock, pkg), hidden, saved.userDock);
    }

    public static State reset() {
        return State.fresh();
    }

    public static String encode(State state) {
        State saved = state == null ? State.fresh() : state;
        List<String> hidden = new ArrayList<>(saved.hidden);
        Collections.sort(hidden);
        return "1\n" + (saved.userDock ? "1" : "0") + "\n"
                + join(saved.order) + "\n"
                + join(saved.dock) + "\n"
                + join(hidden);
    }

    public static State decode(String raw) {
        if (raw == null || raw.isEmpty()) return State.fresh();
        String[] lines = raw.split("\n", -1);
        if (lines.length < 5 || !"1".equals(lines[0])) return State.fresh();
        return new State(split(lines[2]), split(lines[3]), new LinkedHashSet<>(split(lines[4])), "1".equals(lines[1]));
    }

    private static List<String> orderWithPins(List<String> pins, List<String> shown) {
        List<String> order = new ArrayList<>(pins);
        for (String pkg : shown) {
            if (!order.contains(pkg)) order.add(pkg);
        }
        return order;
    }

    private static List<String> without(List<String> pkgs, String pkg) {
        List<String> out = new ArrayList<>();
        for (String other : pkgs) {
            if (!other.equals(pkg)) out.add(other);
        }
        return out;
    }

    private static int compareLabel(App a, App b) {
        int byLabel = a.label.compareToIgnoreCase(b.label);
        if (byLabel != 0) return byLabel;
        return a.pkg.compareTo(b.pkg);
    }

    private static List<String> copy(List<String> pkgs) {
        return pkgs == null ? new ArrayList<>() : new ArrayList<>(pkgs);
    }

    private static String join(List<String> pkgs) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < pkgs.size(); i++) {
            if (i > 0) out.append(',');
            out.append(pkgs.get(i));
        }
        return out.toString();
    }

    private static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        if (line == null || line.isEmpty()) return out;
        for (String part : line.split(",")) {
            if (!part.isEmpty()) out.add(part);
        }
        return out;
    }
}
