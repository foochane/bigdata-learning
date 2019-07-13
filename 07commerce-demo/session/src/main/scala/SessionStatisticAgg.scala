import commons.conf.ConfigurationManager
import commons.constant.Constants
import net.sf.json.JSONObject



/**
  * Created by fucheng on 2019/7/13.
  */
object SessionStatisticAgg {
  def main(args: Array[String]): Unit = {
     var jsonStr = ConfigurationManager.config.getString(Constants.TASK_PARAMS)
     var taskParam = JSONObject.fromObject(jsonStr)
  }

}
