package com.example.flipmap
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.core.content.contentValuesOf
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.abs

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var mRoute: ShapeRenderer
    // this gets set in onSurfaceChanged
    private var ratio: Float = 1f
    private var route_coordinates = FloatArray(0) // TODO make these actual coordinates
    private var route_gl_coordinates = FloatArray(0) // this holds the coordinates, but 0.0-1.0
    private var route_needs_update = true
    private var user_location = FloatArray(3)
    private val rotationMatrix = FloatArray(16)
    val SCALE = 150

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        // initialize route
        mRoute = ShapeRenderer()
    }

    // probably will do a lot of work here
    override fun onDrawFrame(unused: GL10) {
        if (route_needs_update) {
            mRoute.setVertices(route_coordinates)
            route_needs_update = false
        }
        // Create a rotation transformation for the route
        val scratch = FloatArray(16)
        // zoom can't currently be done in ortho mode?
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Log.d("Debug", "route len: ${(route_coordinates.size)}")
        Log.d("Debug", "route coords: ${route_coordinates[0]}, ${route_coordinates[1]}")
        Log.d("Debug", "scaled coordinates: ${user_location[1] * SCALE}, ${user_location[0] * SCALE}")
        Matrix.setLookAtM(
            viewMatrix, 0,
            user_location[1] * SCALE, user_location[0] * SCALE, 1f,
            user_location[1] * SCALE, user_location[0] * SCALE, 0f,
            0f, 1f, 0f
        )
        // Matrix.setLookAtM(
        //     viewMatrix, 0,
        //     route_coordinates[0] , route_coordinates[1], 4f,
        //     route_coordinates[0] , route_coordinates[1] , 0f,
        //     0f, 1f, 0f
        // )

        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -10f, 10f, 1f, 1000f)

        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        Matrix.setRotateM(rotationMatrix, 0, 0f, 0f, 0f, -1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)
        mRoute.draw(scratch)
    }
    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    // called on view resize
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        ratio = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        // Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -10f, 10f, 1f, 1000f)
    }
    // Called by main activity to send new route information
    fun <T: Number> setRouteCoordinates(coords: Array<Pair<T, T>>) {
        val refLat = coords[0].first.toFloat()
        val refLon = coords[0].second.toFloat()
        // initialize coordinates to their offset from the beginning of the route
        route_coordinates = coords.flatMap { (a, b) -> listOf(a.toFloat() * SCALE, b.toFloat() * SCALE, 0.0f) }.toFloatArray()
        if (::mRoute.isInitialized) {
            mRoute.setVertices(route_coordinates)
        }
        else {
            route_needs_update = true
        }
    }
    // Called by main activity to send a new GPS update
    fun setLocation(coordinate: FloatArray) {
        user_location = coordinate
    }
}

// DISCLAIMER chatGPT wrote this class for me
class ShapeRenderer {
    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private var mProgram: Int
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var vertexBuffer: FloatBuffer? = null
    private var vertexCount: Int = 0
    private var vertexStride: Int = 0

    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    init {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun setVertices(vertices: FloatArray) {
        vertexCount = vertices.size / COORDS_PER_VERTEX
        vertexStride = COORDS_PER_VERTEX * 4

        // initialize the vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)

        mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        }

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, color, 0)
        }

        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}

fun loadShader(type: Int, shaderCode: String): Int {

    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    return GLES20.glCreateShader(type).also { shader ->

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}