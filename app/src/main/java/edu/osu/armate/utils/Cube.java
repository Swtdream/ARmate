/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package edu.osu.armate.utils;

import java.nio.Buffer;


public class Cube extends MeshObject
{

    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;

    private int indicesNumber = 0;
    private int verticesNumber = 0;


    public Cube()
    {
        setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }


    private void setVerts()
    {
        double[] TEAPOT_VERTS = {
                -1.00, -1.00, 1.00, // front
                1.00, -1.00, 1.00,
                1.00, 1.00, 1.00,
                -1.00, 1.00, 1.00,

                -1.00, -1.00, -1.00, // back
                1.00, -1.00, -1.00,
                1.00, 1.00, -1.00,
                -1.00, 1.00, -1.00,

                -1.00, -1.00, -1.00, // left
                -1.00, -1.00, 1.00,
                -1.00, 1.00, 1.00,
                -1.00, 1.00, -1.00,

                1.00, -1.00, -1.00, // right
                1.00, -1.00, 1.00,
                1.00, 1.00, 1.00,
                1.00, 1.00, -1.00,

                -1.00, 1.00, 1.00, // top
                1.00, 1.00, 1.00,
                1.00, 1.00, -1.00,
                -1.00, 1.00, -1.00,

                -1.00, -1.00, 1.00, // bottom
                1.00, -1.00, 1.00,
                1.00, -1.00, -1.00,
                -1.00, -1.00, -1.00 };
        mVertBuff = fillBuffer(TEAPOT_VERTS);
        verticesNumber = TEAPOT_VERTS.length / 3;
    }


    private void setTexCoords()
    {
        double[] TEAPOT_TEX_COORDS = {
                0, 0, 1, 0, 1, 1, 0, 1,

                1, 0, 0, 0, 0, 1, 1, 1,

                0, 0, 1, 0, 1, 1, 0, 1,

                1, 0, 0, 0, 0, 1, 1, 1,

                0, 0, 1, 0, 1, 1, 0, 1,

                1, 0, 0, 0, 0, 1, 1, 1 };
        mTexCoordBuff = fillBuffer(TEAPOT_TEX_COORDS);

    }


    private void setNorms()
    {
        double[] TEAPOT_NORMS = {
                0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,

                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,

                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,

                0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,

                1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,

                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0 };
        mNormBuff = fillBuffer(TEAPOT_NORMS);
    }


    private void setIndices()
    {
        short[] TEAPOT_INDICES = {
                0, 1, 2, 0, 2, 3, // front
                4, 6, 5, 4, 7, 6, // back
                8, 9, 10, 8, 10, 11, // left
                12, 14, 13, 12, 15, 14, // right
                16, 17, 18, 16, 18, 19, // top
                20, 22, 21, 20, 23, 22  // bottom
        };
        mIndBuff = fillBuffer(TEAPOT_INDICES);
        indicesNumber = TEAPOT_INDICES.length;
    }


    public int getNumObjectIndex()
    {
        return indicesNumber;
    }


    @Override
    public int getNumObjectVertex()
    {
        return verticesNumber;
    }


    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
            default:
                break;

        }

        return result;
    }

}
