package com.wecc

object CdxWebService {

  val service = {
    val ctrl = new cdx.NodeFileCtrl
    ctrl.getNodeFileCtrlSoap
  }
}