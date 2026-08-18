package ac.ghost.anticheat;

import cn.nukkit.plugin.PluginBase;

public class GhostPlugin extends PluginBase {
    @Override
    public void onEnable() {
        Ghost.getInstance().init(this);
    }

    @Override
    public void onDisable() {
        Ghost.getInstance().terminate(this);
    }
}
