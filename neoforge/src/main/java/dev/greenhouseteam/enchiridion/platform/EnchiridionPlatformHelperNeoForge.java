import dev.greenhouseteam.enchiridion.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class EnchiridionPlatformHelperNeoForge implements EnchiridionPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }
}