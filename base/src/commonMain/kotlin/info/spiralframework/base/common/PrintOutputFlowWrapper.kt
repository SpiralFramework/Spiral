package info.spiralframework.base.common

import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.PrintOutputFlow

class PrintOutputFlowWrapper(flow: OutputFlow): PrintOutputFlow, OutputFlow by flow