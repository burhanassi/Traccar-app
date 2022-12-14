package com.logestechs.driver.data.model


data class DriverCompanyConfigurations(
    var currency: String? = null,
    var isPartialDeliveryEnabled: Boolean? = null,
    var companyDomain: String? = null,
    var companyType: String? = null,
    var isShowPaymentTypesWhenDriverDeliver: Boolean? = null,
    var isEnableDeliveryVerificationPinCodeForPkgs: Boolean? = null,
    var isEnableDeliveryVerificationPinCodeForPkgsWithCodGreaterThan: Double? = null,
    var hasRouteOptimization: Boolean? = null,
    var isForceDriversAttachDraftPackage: Boolean? = null,
    var isEnableDeletingCustomerAccountByCustomer: Boolean? = null,
    var isDriverCanFailPackageDisabled: Boolean? = null,
    var isSignatureOnPackageDeliveryDisabled: Boolean? = null,
    var isDriverPickupPackagesByScanDisabled: Boolean? = null,
    var isTrucking: Boolean? = null,
    var isDistributor: Boolean? = null,
    var isBundlePodEnabled: Boolean? = null,
    var isDriverCanRequestCodChange: Boolean? = null,
    var isDriverCanReturnPackage: Boolean? = null,
    var isDriverPickupAcceptedPackages: Boolean? = null,
    var isPromptNoteForDriverInPackageDelivery: Boolean? = null,
    var isFulfilmentEnabled: Boolean? = null,
    var isShowDriverLocationInPackageTracking: Boolean? = null,
    var isAllowDriverRejectingOrders: Boolean? = null,
    var isHidePaymentTypesWhenDriverDeliver: Boolean? = null
)