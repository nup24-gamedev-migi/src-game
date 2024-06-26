import com.mygdx.game.Logic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FieldCoverageTests {
  private Logic.CellType[][] genEmptyField(final int width, final int height) {
    final Logic.CellType[][] field = new Logic.CellType[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        field[y][x] = Logic.CellType.FLOOR;
      }
    }

    return field;
  }

  private Logic.MoveDirection[] parseMoveSequence(final String code) {
    final char[] chars = code.toCharArray();
    final Logic.MoveDirection[] dirs = new Logic.MoveDirection[code.length()];

    for (int i = 0; i < dirs.length; i++) {
      switch (chars[i]) {
        case 'U':
          dirs[i] = Logic.MoveDirection.UP;
          break;
        case 'L':
          dirs[i] = Logic.MoveDirection.LEFT;
          break;
        case 'D':
          dirs[i] = Logic.MoveDirection.DOWN;
          break;
        case 'R':
          dirs[i] = Logic.MoveDirection.RIGHT;
          break;
        default:
          throw new IllegalArgumentException(
                  String.format("Illegal char '%c' at %d", chars[i], i)
          );
      }
    }

    return dirs;
  }

  private String encodeShadow(final Logic logic) {
    StringBuilder acc = new StringBuilder();

    for (int y = 0; y < logic.getFieldHeight(); y++) {
      for (int x = 0; x < logic.getFieldWidth(); x++) {
        if (logic.getCell(x, y).hasShadow) {
          acc.append("x");
        } else {
          acc.append("o");
        }
      }

      acc.append("\n");
    }

    return acc.toString();
  }

  private void tryMoves(
          final Logic logic,
          final Logic.MoveDirection[] moves,
          final String shadowExpectation
  ) {
    Arrays.stream(moves).forEach(logic::movePlayer);
    logic.applyShadowToField();

    Assertions.assertEquals(
            "\n" + shadowExpectation.trim(),
            "\n" + encodeShadow(logic).trim()
    );
  }

  private void tryMoves(
          final Logic logic,
          final String movesCode,
          final String shadowExpectation
  ) {
    final Logic.MoveDirection[] moves = parseMoveSequence(movesCode);

    tryMoves(logic, moves, shadowExpectation);
  }

  private void tryMoves(
          final int fieldWidth,
          final int fieldHeight,
          final Logic.Pos playerPos,
          final String movesCode,
          final String shadowExpectation
  ) {
    Assertions.assertNotEquals(fieldHeight, 0);
    Assertions.assertNotEquals(fieldWidth, 0);

    // FIXME we rely on correctness of entrance tiles. Does not seems good
    final Logic.CellType[][] field = genEmptyField(fieldWidth, fieldHeight);
    field[playerPos.y][playerPos.x] = Logic.CellType.ENTRANCE;

    final Logic logic = new Logic(field, new HashMap<>());

    tryMoves(logic, movesCode, shadowExpectation);
  }

  @Test
  public void testCoverSimple() {
    tryMoves(
            8, 8,
            new Logic.Pos(1, 1),
            "RRDD",
            "oooooooo\nooxxoooo\noooxoooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\n"
    );
  }

  @Test
  public void testCoverSimpleCircle() {
    tryMoves(
            8, 7,
            new Logic.Pos(1, 1),
            "RRRDDLLUUU",
            "oooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\n"
    );
  }

  @Test
  public void testLongDistance() {
    tryMoves(
            8, 7,
            new Logic.Pos(0, 0),
            "RRRRRRRDDDDDDLLLLLLLUUUUURRRRRRDDDLLLL",
            "oxxxxxxx\nxxxxxxxx\nxoooooxx\nxoooooxx\nxooxxxxx\nxoooooox\nxxxxxxxx\n"
    );
  }

  @Test
  public void testCoverTwoCircles() {
    tryMoves(8, 8,
            new Logic.Pos(0, 0),
            "RRRDDLLUURRDDDRRDDLLUUR",
            "oooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\n"
    );
  }

  @Test
  public void testSomeDiffFieldSize() {
    tryMoves(8, 4,
            new Logic.Pos(1, 1),
            "RRDD",
            "oooooooo\nooxxoooo\noooxoooo\noooooooo\n"
    );
  }

  @Test
  public void testCoverHugeCircle() {
    tryMoves(8, 8,
            new Logic.Pos(0, 0),
            "RRRRRRRDDDDDDDLLLLLLLUUUUUUURR",
            "oooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\noooooooo\n"
    );
  }

  /* move history for test below
   * s x x x
   * x X X x
   * x _ _ x
   * x x x x
   */
  @Test
  public void testCoverNotValidCircle() {
    tryMoves(4, 4,
            new Logic.Pos(0, 0),
            "RRDLURR",
            "oxxo\noxxo\noooo\noooo\n");
  }

  @Test
  public void testCoverNotValidCircleInCircle() {
    tryMoves(4, 4,
            new Logic.Pos(0, 0),
            "RRDLURRDDDLLLUUURRR",
            "oooo\noxxo\noooo\noooo\n");
  }

}
