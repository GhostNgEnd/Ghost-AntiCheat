package ac.ghost.anticheat.check.api;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.player.GhostPlayer;

public class Check {
    protected final GhostPlayer player;

    private final String name, type;
    private final boolean experimental;
    private int vl = 0;

    public Check(GhostPlayer player) {
        this.player = player;
        CheckInfo info = getClass().getDeclaredAnnotation(CheckInfo.class);
        this.name = info != null ? info.name() : getClass().getSimpleName();
        this.type = info != null ? info.type() : "";
        this.experimental = getClass().getDeclaredAnnotation(Experimental.class) != null;
    }

    public Check(GhostPlayer player, String name, String type, boolean experimental) {
        this.player = player;
        this.name = name;
        this.type = type;
        this.experimental = experimental;
    }

    public void fail() {
        fail("");
    }

    public void fail(String verbose) {
        if (player.isExempted()) {
            return;
        }
        this.vl++;
        final String details = verbose == null || verbose.isBlank() ? "" : " " + verbose;
        Ghost.getInstance().getAlertManager().alert(getDisplayName() + " mitigated for §b" + name + " §7| x" + vl + details);
    }

    protected final int violationLevel() {
        return this.vl;
    }

    protected final String getDisplayName() {
        return player.getSession().getDisplayName();
    }
}
