package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Logic.CellType;

public class View {
    private final ShapeRenderer debugRenderer;
    private final SpriteBatch batch;
    private final Texture playerImg;
    private final Texture boxImg;
    private final Texture grass;
    private final Texture wall;
    private final Texture shadow;
    private static final float sizeOfBlock = 64;

    View() {
        playerImg = new Texture("badlogic.jpg");
        boxImg = new Texture("box.png");
        grass = new Texture("grass.png");
        wall = new Texture("wall.jpg");
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
        shadow = new Texture("shadow-2.png");
    }

    public void view(final Logic model) {
        // start - offset from (0, 0)
        Vector2 start = new Vector2(0, 0);
        int fieldWidth = model.getFieldWidth();
        int fieldHeight = model.getFieldHeight();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

//        ScreenUtils.clear(1, 1, 0, 1);
        drawField(fieldWidth, fieldHeight, start, model, fieldHeight);
//
        batch.begin();
        model.allThings().forEach(entry -> {
            final Logic.Pos lPos = entry.getKey();
            final Vector2 pos = logicToScreen(lPos, fieldHeight, start).sub(
                    new Vector2(0, sizeOfBlock)
            );
            final Logic.ThingType ty = entry.getValue();
            final Texture img;
            switch (ty) {
                case PLAYER:
                    img = playerImg;
                    break;
                case BOX:
                    img = boxImg;
                    break;
                default:
                    img = null;
            }
            final Sprite sprite = new Sprite(img, 64, 64);
            sprite.setSize(64, 64);
            sprite.setPosition(pos.x, pos.y);

            sprite.draw(batch);
        });
        batch.end();

        for (final Logic.Pair pair : model.getHistory()) {
            // pair contains an old pos.
            final Logic.Pos t = pair.pos.applyDir(pair.dir);
            final Logic.Pos oldPos = new Logic.Pos(
                    pair.pos.x - (t.x - pair.pos.x),
                    pair.pos.y - (t.y - pair.pos.y)
            );

            final Vector2 beg = logicToScreen(oldPos, fieldHeight, start)
                    .add(sizeOfBlock / 2, -sizeOfBlock / 2);
            final Vector2 end = logicToScreen(pair.pos, fieldHeight, start)
                    .add(sizeOfBlock / 2, -sizeOfBlock / 2);

            DrawDebugLine(beg, end);
        }
    }

    private void DrawDebugLine(Vector2 start, Vector2 end) {
        Gdx.gl.glLineWidth(2);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.RED);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void drawTexture(CellType type, Vector2 pos) {
        Texture toDraw;
        switch (type) {
            case FLOOR: {
                toDraw = grass;
                break;
            }
            case WALL: {
                toDraw = wall;
                break;
            }
            default: {
                toDraw = playerImg;
                break;
            }

        }
        batch.begin();
        batch.draw(toDraw, pos.x, pos.y);
        batch.end();
    }

    private void drawField(int width, int height, Vector2 start, final Logic logic, int fieldHeight) {
        for (int y = 0; y < logic.getFieldHeight(); y++) {
            for (int x = 0; x < logic.getFieldWidth(); x++) {
                Vector2 currentCellPos = logicToScreen(
                        new Logic.Pos(x, y + 1),
                        fieldHeight,
                        start
                );
                drawTexture(logic.getCell(x, y).type, currentCellPos);
                if (logic.getCell(x, y).hasShadow) { drawShadow(currentCellPos); }
            }
        }
    }

    private void drawShadow(Vector2 pos) {
        batch.begin();
        batch.draw(shadow, pos.x, pos.y);
        batch.end();
    }



    // LibGDX goes from bottom left to top right
    public Vector2 logicToScreen(
            final Logic.Pos lPos,
            final int fieldHeight,
            final Vector2 start
    ) {
        return new Vector2(
                (float)lPos.x,
                (float)(fieldHeight - lPos.y)
        ).scl(sizeOfBlock).add(start);
    }

    public void dispose() {
        debugRenderer.dispose();
        batch.dispose();
    }
}
