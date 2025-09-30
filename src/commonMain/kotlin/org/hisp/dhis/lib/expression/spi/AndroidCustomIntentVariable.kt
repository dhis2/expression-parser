package org.hisp.dhis.lib.expression.spi

import kotlin.js.JsExport

@JsExport
enum class AndroidCustomIntentVariable {
    orgunit_code,
    orgunit_id,
    orgunit_path,
    user_id,
    user_username
}
