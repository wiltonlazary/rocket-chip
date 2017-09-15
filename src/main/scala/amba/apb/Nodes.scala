// See LICENSE.SiFive for license details.

package freechips.rocketchip.amba.apb

import Chisel._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._

object APBImp extends NodeImp[APBMasterPortParameters, APBSlavePortParameters, APBEdgeParameters, APBEdgeParameters, APBBundle]
{
  def edgeO(pd: APBMasterPortParameters, pu: APBSlavePortParameters): APBEdgeParameters = APBEdgeParameters(pd, pu)
  def edgeI(pd: APBMasterPortParameters, pu: APBSlavePortParameters): APBEdgeParameters = APBEdgeParameters(pd, pu)

  def bundleO(eo: APBEdgeParameters): APBBundle = APBBundle(eo.bundle)
  def bundleI(ei: APBEdgeParameters): APBBundle = APBBundle(ei.bundle)

  def colour = "#00ccff" // bluish
  override def labelI(ei: APBEdgeParameters) = (ei.slave.beatBytes * 8).toString
  override def labelO(eo: APBEdgeParameters) = (eo.slave.beatBytes * 8).toString

  override def mixO(pd: APBMasterPortParameters, node: OutwardNode[APBMasterPortParameters, APBSlavePortParameters, APBBundle]): APBMasterPortParameters  =
   pd.copy(masters = pd.masters.map  { c => c.copy (nodePath = node +: c.nodePath) })
  override def mixI(pu: APBSlavePortParameters, node: InwardNode[APBMasterPortParameters, APBSlavePortParameters, APBBundle]): APBSlavePortParameters =
   pu.copy(slaves  = pu.slaves.map { m => m.copy (nodePath = node +: m.nodePath) })
}

// Nodes implemented inside modules
case class APBIdentityNode()(implicit valName: ValName) extends IdentityNode(APBImp)
case class APBMasterNode(portParams: Seq[APBMasterPortParameters])(implicit valName: ValName) extends SourceNode(APBImp)(portParams)
case class APBSlaveNode(portParams: Seq[APBSlavePortParameters])(implicit valName: ValName) extends SinkNode(APBImp)(portParams)
case class APBNexusNode(
  masterFn:       Seq[APBMasterPortParameters] => APBMasterPortParameters,
  slaveFn:        Seq[APBSlavePortParameters]  => APBSlavePortParameters,
  numMasterPorts: Range.Inclusive = 1 to 1,
  numSlavePorts:  Range.Inclusive = 1 to 1)(
  implicit valName: ValName)
  extends NexusNode(APBImp)(masterFn, slaveFn, numMasterPorts, numSlavePorts)

// Nodes passed from an inner module
case class APBOutputNode()(implicit valName: ValName) extends OutputNode(APBImp)
case class APBInputNode()(implicit valName: ValName) extends InputNode(APBImp)

// Nodes used for external ports
case class APBBlindOutputNode(portParams: Seq[APBSlavePortParameters])(implicit valName: ValName) extends BlindOutputNode(APBImp)(portParams)
case class APBBlindInputNode(portParams: Seq[APBMasterPortParameters])(implicit valName: ValName) extends BlindInputNode(APBImp)(portParams)

case class APBInternalOutputNode(portParams: Seq[APBSlavePortParameters])(implicit valName: ValName) extends InternalOutputNode(APBImp)(portParams)
case class APBInternalInputNode(portParams: Seq[APBMasterPortParameters])(implicit valName: ValName) extends InternalInputNode(APBImp)(portParams)
