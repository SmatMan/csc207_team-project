package view.components;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Downloads images (e.g. recipe photos) in the background and caches them, so the
 * window never freezes while a photo loads.
 */
public final class RemoteImages {

    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService DOWNLOADS = Executors.newFixedThreadPool(3, task -> {
        final Thread thread = new Thread(task, "image-download");
        // Don't keep the app running just because a download is in progress.
        thread.setDaemon(true);
        return thread;
    });

    private RemoteImages() {
    }

    /**
     * Loads an image and hands it to {@code onLoaded} on the Swing thread. If the address
     * is blank or the download fails, {@code onLoaded} is never called.
     * @param url the image's address
     * @param onLoaded what to do with the image once it arrives
     */
    public static void load(String url, Consumer<BufferedImage> onLoaded) {
        if (url == null || url.isBlank()) {
            return;
        }
        final BufferedImage cached = CACHE.get(url);
        if (cached != null) {
            onLoaded.accept(cached);
            return;
        }
        DOWNLOADS.submit(() -> {
            try {
                final BufferedImage image = ImageIO.read(URI.create(url).toURL());
                if (image != null) {
                    CACHE.put(url, image);
                    SwingUtilities.invokeLater(() -> onLoaded.accept(image));
                }
            }
            catch (IOException | IllegalArgumentException ex) {
                // Leave the placeholder in place; a missing photo isn't worth an error message.
            }
        });
    }
}
