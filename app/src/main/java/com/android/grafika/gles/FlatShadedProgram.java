package com.android.grafika.gles;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * GL program and supporting functions for flat-shaded rendering.
 */
public class FlatShadedProgram {
    private static final String TAG = GlUtil.TAG;

    // Vertex shader with added support for texture coordinates
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec2 aTexCoord;" +          // Added line
                    "varying vec2 vTexCoord;" +            // Added line
                    "void main() {" +
                    "    gl_Position = uMVPMatrix * aPosition;" +
                    "    vTexCoord = aTexCoord;" +         // Added line
                    "}";

    // Fragment shader with added support for textures
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform vec4 uColor;" +
                    "uniform sampler2D uTexture;" +        // Added line
                    "uniform bool useTexture;" +           // Added line
                    "varying vec2 vTexCoord;" +            // Added line
                    "void main() {" +
                    "    if (useTexture) {" +              // Added lines
                    "        gl_FragColor = texture2D(uTexture, vTexCoord);" +  // Added line
                    "    } else {" +
                    "        gl_FragColor = uColor;" +
                    "    }" +
                    "}";

    // Handles to the GL program and various components of it.
    private int mProgramHandle = -1;
    private int muColorLoc = -1;
    private int muMVPMatrixLoc = -1;
    private int maPositionLoc = -1;
    private int maTexCoordLoc = -1;  // Added line
    private int muTextureLoc = -1;   // Added line
    private int muUseTextureLoc = -1;  // Added line

    /**
     * Prepares the program in the current EGL context.
     */
    public FlatShadedProgram() {
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        Log.d(TAG, "Created program " + mProgramHandle);

        // Get locations of attributes and uniforms
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTexCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTexCoord");  // Added line
        GlUtil.checkLocation(maTexCoordLoc, "aTexCoord");                        // Added line
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muColorLoc = GLES20.glGetUniformLocation(mProgramHandle, "uColor");
        GlUtil.checkLocation(muColorLoc, "uColor");
        muTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexture");  // Added line
        GlUtil.checkLocation(muTextureLoc, "uTexture");                        // Added line
        muUseTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "useTexture");  // Added line
        GlUtil.checkLocation(muUseTextureLoc, "useTexture");                   // Added line
    }

    /**
     * Releases the program.
     */
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /**
     * Draws a rectangle.
     */
    public void drawRectangle(float[] mvpMatrix, float[] color, FloatBuffer vertexBuffer,
                              int firstVertex, int vertexCount, int coordsPerVertex, int vertexStride) {
        draw(mvpMatrix, color, vertexBuffer, null, firstVertex, vertexCount, coordsPerVertex, vertexStride, 0, false, 0);  // Modified line
    }

    /**
     * Draws a texture.
     */
    public void drawTexture(float[] mvpMatrix, FloatBuffer vertexBuffer, FloatBuffer texBuffer,
                            int firstVertex, int vertexCount, int coordsPerVertex, int vertexStride, int texStride, int textureId) {
        draw(mvpMatrix, null, vertexBuffer, texBuffer, firstVertex, vertexCount, coordsPerVertex, vertexStride, texStride, true, textureId);  // Modified line
    }

    /**
     * Issues the draw call. Does the full setup on every call.
     *
     * @param mvpMatrix The 4x4 projection matrix.
     * @param color A 4-element color vector.
     * @param vertexBuffer Buffer with vertex data.
     * @param texBuffer Buffer with texture data (if drawing a texture).
     * @param firstVertex Index of first vertex to use in vertexBuffer.
     * @param vertexCount Number of vertices in vertexBuffer.
     * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
     * @param vertexStride Width, in bytes, of the data for each vertex (often vertexCount * sizeof(float)).
     * @param texStride Width, in bytes, of the data for each texture coordinate (often 2 * sizeof(float)).
     * @param useTexture Flag indicating whether to use a texture.
     * @param textureId The OpenGL texture ID to use (if drawing a texture).
     */
    private void draw(float[] mvpMatrix, float[] color, FloatBuffer vertexBuffer, FloatBuffer texBuffer,
                      int firstVertex, int vertexCount, int coordsPerVertex, int vertexStride,
                      int texStride, boolean useTexture, int textureId) {
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Indicate whether to use a texture
        GLES20.glUniform1i(muUseTextureLoc, useTexture ? 1 : 0);  // Added line
        GlUtil.checkGlError("glUniform1i");

        if (useTexture) {
            // Enable the "aTexCoord" vertex attribute.
            GLES20.glEnableVertexAttribArray(maTexCoordLoc);  // Added line
            GlUtil.checkGlError("glEnableVertexAttribArray");

            // Connect texBuffer to "aTexCoord".
            GLES20.glVertexAttribPointer(maTexCoordLoc, 2, GLES20.GL_FLOAT, false, texStride, texBuffer);  // Added line
            GlUtil.checkGlError("glVertexAttribPointer");

            // Bind the texture.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  // Added line
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);  // Added line
            GLES20.glUniform1i(muTextureLoc, 0);  // Added line
        } else {
            // Copy the color vector in.
            GLES20.glUniform4fv(muColorLoc, 1, color, 0);
            GlUtil.checkGlError("glUniform4fv");
        }

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        if (useTexture) {
            GLES20.glDisableVertexAttribArray(maTexCoordLoc);  // Added line
        }
        GLES20.glUseProgram(0);
    }
}
