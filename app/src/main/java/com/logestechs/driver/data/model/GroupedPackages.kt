package com.logestechs.driver.data.model

data class GroupedPackages(
    var label: String?,
    var pkgs: ArrayList<Package?>?,
    @Transient
    var isExpanded: Boolean = false
)