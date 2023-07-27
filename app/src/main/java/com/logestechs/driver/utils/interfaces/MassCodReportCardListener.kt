package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.GroupedMassCodReports
import com.logestechs.driver.data.model.MassCodReport

interface MassCodReportCardListener {
    fun onDeliverMassReport(index: Int, massCodReport: MassCodReport?)
    fun onDeliverGroupReport(index: Int, groupedPackage: GroupedMassCodReports?)

}
