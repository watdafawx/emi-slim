package dev.watdafawx.emislim;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = EmiSlim.MOD_ID, dist = Dist.CLIENT)
public final class EmiSlim {
    public static final String MOD_ID = "emislim";
    public static final Logger LOG = LoggerFactory.getLogger("EMI Slim");

    public EmiSlim() {
        LOG.info("EMI Slim loaded: shareTags={}, internPatches={}", TagShare.ENABLED, PatchInterner.ENABLED);
    }

    public static void logStats() {
        LOG.info("EMI Slim: {}; {}", TagShare.stats(), PatchInterner.stats());
    }
}
