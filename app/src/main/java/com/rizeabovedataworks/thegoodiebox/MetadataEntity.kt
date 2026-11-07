package com.rizeabovedataworks.thegoodiebox

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_metadata")
data class MetadataEntity(
    @PrimaryKey
    val key: String = "dataset_version",
    val datasetVersion: Int
)
