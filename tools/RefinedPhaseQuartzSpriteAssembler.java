import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/** Rebuilds the animated refined phase quartz texture from its numbered frames. */
public final class RefinedPhaseQuartzSpriteAssembler {
    private static final int FRAME_COUNT = 12;
    private static final String DIRECTORY =
        "src/main/resources/assets/techcraft/textures/item/refined_phase_quartz/";

    private RefinedPhaseQuartzSpriteAssembler() {}

    public static void main(String[] args) throws Exception {
        BufferedImage firstFrame = readFrame(0);
        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();
        BufferedImage spriteSheet = new BufferedImage(
            width, height * FRAME_COUNT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = spriteSheet.createGraphics();
        try {
            for (int index = 0; index < FRAME_COUNT; index++) {
                BufferedImage frame = index == 0 ? firstFrame : readFrame(index);
                if (frame.getWidth() != width || frame.getHeight() != height) {
                    throw new IllegalStateException("Frame " + index + " has a different size");
                }
                graphics.drawImage(frame, 0, index * height, null);
            }
        } finally {
            graphics.dispose();
        }

        File output = new File(DIRECTORY + "refined_phase_quartz.png");
        System.out.println("Writing " + output.getCanonicalPath());
        if (!ImageIO.write(spriteSheet, "png", output)) {
            throw new IllegalStateException("No PNG writer is available");
        }
        BufferedImage written = ImageIO.read(output);
        if (written.getWidth() != width || written.getHeight() != height * FRAME_COUNT) {
            throw new IllegalStateException(
                "Written sprite sheet has unexpected size: "
                    + written.getWidth() + "x" + written.getHeight());
        }
        System.out.println("Wrote " + written.getWidth() + "x" + written.getHeight());
    }

    private static BufferedImage readFrame(int index) throws Exception {
        return ImageIO.read(new File(DIRECTORY + "refined_phase_quartz_" + index + ".png"));
    }
}
