import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;


import java.util.Random;

public class SteveBot extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new SteveBot().start();
    }

    // Constructor, which loads the bot config file
    SteveBot() {
        super(BotInfo.fromFile("SteveBot.json"));
    }

    // Random generator for movement
    private final Random random = new Random();

    // Battlefield dimensions (will be set on first tick)
    private double fieldWidth = 0;
    private double fieldHeight = 0;

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Get battlefield size
        fieldWidth = getArenaWidth();
        fieldHeight = getArenaHeight();

        while (isRunning()) {
            // Randomize speed and direction
            double speed = getBalancedSpeed();
            int direction = random.nextBoolean() ? 1 : -1;
            int turn = random.nextInt(90) - 45; // -45 to +44 degrees

            // Move and turn
            turnRight(turn);
            if (direction > 0) {
                forward(speed);
            } else {
                back(speed);
            }

            // Scan for bots
            turnGunRight(360);

            // Wall avoidance
            avoidWalls();
        }
    }

    // Wall avoidance: if close to wall, turn away
    private void avoidWalls() {
        double margin = 60; // pixels
        double x = getX();
        double y = getY();
        double heading = getDirection();
        double moveDistance = 80; // how far we want to move

        // Predict next position
        double radians = Math.toRadians(heading);
        double nextX = x + Math.cos(radians) * moveDistance;
        double nextY = y + Math.sin(radians) * moveDistance;

        // If next position is too close to a wall, turn away
        if (nextX < margin || nextX > fieldWidth - margin || nextY < margin || nextY > fieldHeight - margin) {
            // Turn away from wall
            int turnAngle = 90 + random.nextInt(90); // 90-179 degrees
            turnRight(turnAngle);
            // Optionally, move a bit backward to get away from wall
            back(40 + random.nextInt(40));
        }
    }

    // Speed/armor balancing: move faster with more armor
    private double getBalancedSpeed() {
        double armor = getEnergy();
        if (armor > 80) return 120 + random.nextInt(40);
        if (armor > 50) return 80 + random.nextInt(30);
        if (armor > 20) return 50 + random.nextInt(20);
        return 30 + random.nextInt(10);
    }

    // We saw another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Fire with power based on distance and our energy
        // Calculate Euclidean distance to scanned bot
        double dx = getX() - e.getX();
        double dy = getY() - e.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double power = Math.max(0.1, Math.min(3.0, (getEnergy() / 100.0) * (400.0 / (distance + 1))));
        fire(power);
    }

    // We were hit by a bullet -> random evasive maneuver
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Randomly turn and move to evade
        int evadeTurn = 60 + random.nextInt(60); // 60-119 degrees
        if (random.nextBoolean()) {
            turnRight(evadeTurn);
        } else {
            turnLeft(evadeTurn);
        }
        if (random.nextBoolean()) {
            forward(getBalancedSpeed());
        } else {
            back(getBalancedSpeed());
        }
    }
}
