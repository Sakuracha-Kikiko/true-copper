package com.sakuracha.truecopper.maid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.mojang.logging.LogUtils;
import com.sakuracha.truecopper.TrueCopperConfig;
import org.slf4j.Logger;

/**
 * Registers the Zenith Sword work mode. Touhou Little Maid instantiates every {@link LittleMaidExtension}
 * class it finds in the scan data and lets it add tasks before the task registry is frozen.
 */
@LittleMaidExtension
public class ZenithMaidExtension implements ILittleMaid {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void addMaidTask(TaskManager manager) {
        if (!TrueCopperConfig.enableMaidTask()) {
            LOGGER.info("Zenith Sword maid work mode is disabled by the config");
            return;
        }
        try {
            manager.add(new TaskZenithAttack());
            LOGGER.info("Registered maid work mode {}", TaskZenithAttack.UID);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to register the Zenith Sword maid work mode", throwable);
        }
    }
}
