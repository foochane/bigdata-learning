package com.foochane.mapreduce.flow;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * 本案例的功能：演示自定义数据类型如何实现hadoop的序列化接口
 * 1、该类一定要保留空参构造函数
 * 2、write方法中输出字段二进制数据的顺序  要与  readFields方法读取数据的顺序一致
 * 
 */
public class FlowBean implements Writable {

	private int upFlow;
	private int dFlow;
	private String phone;
	private int amountFlow;

	public FlowBean(){}
	
	public FlowBean(String phone, int upFlow, int dFlow) {
		this.phone = phone;
		this.upFlow = upFlow;
		this.dFlow = dFlow;
		this.amountFlow = upFlow + dFlow;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getUpFlow() {
		return upFlow;
	}

	public void setUpFlow(int upFlow) {
		this.upFlow = upFlow;
	}

	public int getdFlow() {
		return dFlow;
	}

	public void setdFlow(int dFlow) {
		this.dFlow = dFlow;
	}

	public int getAmountFlow() {
		return amountFlow;
	}

	public void setAmountFlow(int amountFlow) {
		this.amountFlow = amountFlow;
	}

	/**
	 * hadoop系统在序列化该类的对象时要调用的方法
	 */
	@Override
	public void write(DataOutput out) throws IOException {

		out.writeInt(upFlow);
		out.writeUTF(phone);
		out.writeInt(dFlow);
		out.writeInt(amountFlow);

	}

	/**
	 * hadoop系统在反序列化该类的对象时要调用的方法
	 */
	@Override
	public void readFields(DataInput in) throws IOException {
		this.upFlow = in.readInt();
		this.phone = in.readUTF();
		this.dFlow = in.readInt();
		this.amountFlow = in.readInt();
	}

	@Override
	public String toString() {
		 
		return this.phone + ","+this.upFlow +","+ this.dFlow +"," + this.amountFlow;
	}
	
}
