/**
 * Copyright (c) 2013 Center for eHalsa i samverkan (CeHis).
 * 							<http://cehis.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.agp.test.producer;

import javax.xml.datatype.XMLGregorianCalendar;

public class VagvalMockInputRecord {

	public String senderId;
	public String receiverId;
	public String rivVersion;
	public String serviceContractNamespace;

	public String adress;
	public boolean addVagval=true;
	public boolean addBehorighet=true;
	private XMLGregorianCalendar fromDate;
	private XMLGregorianCalendar toDate;

	public XMLGregorianCalendar getFromDate() {
		return fromDate;
	}

	public void setFromDate(XMLGregorianCalendar fromDate) {
		this.fromDate = fromDate;
	}

	public XMLGregorianCalendar getToDate() {
		return toDate;
	}

	public void setToDate(XMLGregorianCalendar toDate) {
		this.toDate = toDate;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public String getRivVersion() {
		return rivVersion;
	}

	public void setRivVersion(String rivVersion) {
		this.rivVersion = rivVersion;
	}

	public String getServiceContractNamespace() {
		return serviceContractNamespace;
	}

	public void setServiceContractNamespace(String serviceContractNamespace) {
		this.serviceContractNamespace = serviceContractNamespace;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public boolean isAddVagval() {
		return addVagval;
	}

	public void setAddVagval(boolean addVagval) {
		this.addVagval = addVagval;
	}

	public boolean isAddBehorighet() {
		return addBehorighet;
	}

	public void setAddBehorighet(boolean addBehorighet) {
		this.addBehorighet = addBehorighet;
	}

	@Override
	public String toString() {
		return super.toString() + " " + adress;
	}

}
