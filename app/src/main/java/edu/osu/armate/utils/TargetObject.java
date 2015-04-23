package edu.osu.armate.utils;

import java.nio.Buffer;

/**
 * Created by 陈英硕 on 2015/4/19.
 */
public class TargetObject extends MeshObject {

    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;

    private int indicesNumber = 0;
    private int verticesNumber = 0;

    public TargetObject(ARObject obj) {
        indicesNumber = obj.indicesNumber;
        verticesNumber = obj.verticesNumber;
        mVertBuff = fillBuffer(obj.vertices);
        mTexCoordBuff = fillBuffer(obj.textures);
        mNormBuff = fillBuffer(obj.normals);
        mIndBuff = fillBuffer(obj.indices);
    }


    @Override
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
