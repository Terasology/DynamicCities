// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.districts;


import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.utilities.Toolbox;

import java.util.ArrayList;
import java.util.List;


public class Kmeans {

    private List<Vector2i> clusterCenters;
    private boolean initialised;
    private final Logger logger = LoggerFactory.getLogger(Kmeans.class);

    public Kmeans() {
        initialised = false;
    }

    /**
     * @param map: rows are datapoints, columns are variables: x-pos, y-pos, probabilities
     * @param clusters: Count of clusters
     * @return
     */
    public int[] kmeans(float[][] map, int clusters) {
        /**
         * Initialisierung: Wähle k zufällige Mittelwerte (Means) aus dem Datensatz.
         * Zuordnung: Jedes Datenobjekt wird demjenigen Cluster zugeordnet, bei dem die Cluster-Varianz am wenigsten 
         * erhöht wird.
         * Aktualisieren: Berechne die Mittelpunkte der Cluster neu
         * Weightvectors are used as means
         * For better results, start at distinct bfc's
         */
        if (map == null || map[0] == null) {
            throw new NullPointerException();
        }
        int index = 0;
        int points = map.length;
        int variables = map[0].length;
        int[] clusterMap = new int[points];
        float[] clusterSum = new float[clusters];
        float[][] clusterVector = new float[clusters][variables];
        float temp = 0;
        float[] tempvec;
        float min;
        boolean change = true;
        float[][] means = new float[clusters][variables];
        clusterCenters = new ArrayList<>();
        int[] clusterPoints = new int[clusters];

        //Initialize Cluster Map
        for (int i = 0; i < points; i++) {
            clusterMap[i] = clusters * 2;
        }
        //Initializing with random means
        for (int i = 0; i < clusters; i++) {
            int randy = (int) Math.round(Math.random() * (points - 1));
            System.arraycopy(map[randy], 0, means[i], 0, variables);
        }
        int iter = 0;
        while (change && iter < 1000) {
            iter++;
            change = false;
            //assign each point to a cluster
            for (int x = 0; x < points; x++) {

                temp = Toolbox.distance(map[x], means[0]);
                min = temp;
                index = 0;
                for (int i = 1; i < clusters; i++) {

                    temp = Toolbox.distance(map[x], means[i]);
                    if (temp < min) {
                        min = temp;
                        index = i;
                    }

                }
                if (clusterMap[x] != index) {
                    change = true;
                }
                clusterMap[x] = index;
            }
        }
        //Reset neuron count and cluster sum for each cluster
        for (int i = 0; i < clusters; i++) {
            clusterSum[i] = 0;
            for (int j = 0; j < variables; j++) {
                clusterVector[i][j] = 0;
            }
        }
        //Calculate Cluster Means anew

        for (int x = 0; x < points; x++) {
            index = clusterMap[x];
            clusterPoints[index]++;
            for (int j = 0; j < variables; j++) {
                clusterVector[index][j] += map[x][j];
            }
            clusterSum[index] += Toolbox.abs(map[x]);
        }

        for (int i = 0; i < clusters; i++) {
            for (int j = 0; j < variables; j++) {
                if (clusterSum[i] == 0) {
                    logger.debug("No element found for cluster " + i);
                    break;
                }

                means[i][j] = (1 / clusterSum[i]) * clusterVector[i][j];
            }
            Vector2i clusterPosition = new Vector2i((int) clusterVector[i][0], (int) clusterVector[i][1]);
            if (clusterPoints[i] != 0) {
                Vector2i toAdd = new Vector2i(clusterPosition);
                toAdd.x /= clusterPoints[i];
                toAdd.y /= clusterPoints[i];
                clusterCenters.add(i, toAdd);
            } else {
                clusterCenters.add(i, clusterPosition);
            }
        }
        initialised = true;
        return clusterMap;
    }

    public int[] kmeans(List<List<Float>> data, int clusters) {
        float[][] arrayData = new float[data.size()][data.get(0).size()];
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(0).size(); j++) {
                arrayData[i][j] = data.get(i).get(j);
            }
        }
        return kmeans(arrayData, clusters);
    }

    public List<Vector2i> getCenters() {
        if (initialised) {
            return clusterCenters;
        }
        logger.error("Cannot retrieve cluster centers: clusters not initialised");
        return null;
    }
}

