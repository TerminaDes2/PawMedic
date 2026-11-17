import com.pawsmedic.desktop.admin.translation.LocalTranslationKt;
import java.io.File;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;

public class BundleSmoke {
    public static void main(String[] args) throws Exception {
        File directory = LocalTranslationKt.bundledTranslationDirectory();
        if (directory == null || !directory.toPath().toRealPath().equals(new File(args[0]).toPath().toRealPath())) {
            throw new IllegalStateException("Bundled translation directory not found");
        }
        String result = BuildersKt.runBlocking(
            EmptyCoroutineContext.INSTANCE,
            (scope, continuation) -> LocalTranslationKt.translateLocally("El perro tiene fiebre.", continuation)
        );
        if (!result.toLowerCase().contains("dog") || !result.toLowerCase().contains("fever")) {
            throw new IllegalStateException("Unexpected translation");
        }
        System.out.println("Bundled translation smoke test passed");
    }
}
