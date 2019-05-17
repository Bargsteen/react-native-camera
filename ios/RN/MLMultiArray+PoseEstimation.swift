////
////  MLMultiArray+PoseEstimation.swift
////  react-native-camera
////
////  Created by Kasper Dissing Bargsteen on 11/05/2019.
////

import Foundation

import CoreML


@available(iOS 11.0, *)
extension MLMultiArray {
    
    func convertHeatmapToBodyPoints() -> [[Int]] {
        guard self.shape.count >= 3 else {
            print("heatmap's shape is invalid. \(self.shape)")
            return []
        }
        let keypoint_number = self.shape[0].intValue
        let heatmap_w = self.shape[1].intValue
        let heatmap_h = self.shape[2].intValue
        
        var n_kpoints = Array(repeating: Array(repeating: -1, count: 2), count: 14)
        
        for bodypart in 0..<keypoint_number {
            var maxConfidence : Double = 0;
            for row in 1..<heatmap_w-1 {
                for col in 1..<heatmap_h-1 {
                    
                    let confidence = getGaussian(heatmap: self, bodypart, row, col)
                    
                    guard confidence > 0 else { continue }
                    
                    if n_kpoints[bodypart] == [-1, -1] || maxConfidence < confidence {
                        // Flipped "back" on purpose, as the output from the model is flipped
                        n_kpoints[bodypart] = [col,row]
                        maxConfidence = confidence
                    }
                }
            }
        }
        
        return n_kpoints
    }
}

let GAUSSIAN_WEIGHT = 0.2

@available(iOS 11.0, *)
func getGaussian(heatmap: MLMultiArray, _ bodypart: Int, _ row: Int, _ col: Int) -> Double {
    var sum : Double = 0;
    
    let heatmap_w = heatmap.shape[1].intValue
    let heatmap_h = heatmap.shape[2].intValue
    
    //Iterating over the pixels adjacent to current.
    for i in -1..<2 {
        for j in -1..<2 {
            let currentRow = row + i
            let currentCol = col + j
            
            let index = bodypart*(heatmap_w*heatmap_h) + currentRow*(heatmap_h) + currentCol
            let value = heatmap[index].doubleValue
            let weight = i == 0 && j == 0 ? 1 : GAUSSIAN_WEIGHT
            
            sum += value * weight
        }
    }
    return sum
}
