package edu.osu.armate.utils;

/**
 * Created by 陈英硕 on 2015/4/19.
 */
public class ARObject {

    int verticesNumber = 0;
    int indicesNumber = 0;
    double[] vertices;
    double[] textures;
    double[] normals;
    short[] indices;

    public ARObject(double[] vertices, double[] textures, double[] normals, short[] indices) {
        this.vertices = vertices;
        this.textures = textures;
        this.normals = normals;
        this.indices = indices;
        verticesNumber = vertices.length  / 3;
        indicesNumber = indices.length;

    }
}
