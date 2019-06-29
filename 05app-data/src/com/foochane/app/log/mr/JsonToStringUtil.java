package com.foochane.app.log.mr;

import com.alibaba.fastjson.JSONObject;

public class JsonToStringUtil {

	public static String toString(JSONObject jsonObj) {

		StringBuilder sb = new StringBuilder();
		sb.append(jsonObj.get("sdk_ver")).append("\001").append(jsonObj.get("time_zone")).append("\001")
				.append(jsonObj.get("commit_id")).append("\001").append(jsonObj.get("commit_time")).append("\001")
				.append(jsonObj.get("pid")).append("\001").append(jsonObj.get("app_token")).append("\001")
				.append(jsonObj.get("app_id")).append("\001").append(jsonObj.get("device_id")).append("\001")
				.append(jsonObj.get("device_id_type")).append("\001").append(jsonObj.get("release_channel"))
				.append("\001").append(jsonObj.get("app_ver_name")).append("\001").append(jsonObj.get("app_ver_code"))
				.append("\001").append(jsonObj.get("os_name")).append("\001").append(jsonObj.get("os_ver"))
				.append("\001").append(jsonObj.get("language")).append("\001").append(jsonObj.get("country"))
				.append("\001").append(jsonObj.get("manufacture")).append("\001").append(jsonObj.get("device_model"))
				.append("\001").append(jsonObj.get("resolution")).append("\001").append(jsonObj.get("net_type"))
				.append("\001").append(jsonObj.get("account")).append("\001").append(jsonObj.get("app_device_id"))
				.append("\001").append(jsonObj.get("mac")).append("\001").append(jsonObj.get("android_id"))
				.append("\001").append(jsonObj.get("imei")).append("\001").append(jsonObj.get("cid_sn")).append("\001")
				.append(jsonObj.get("build_num")).append("\001").append(jsonObj.get("mobile_data_type")).append("\001")
				.append(jsonObj.get("promotion_channel")).append("\001").append(jsonObj.get("carrier")).append("\001")
				.append(jsonObj.get("city")).append("\001").append(jsonObj.get("user_id"));
		
		return sb.toString();

	}

}
