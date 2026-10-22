package com.hd.nepm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package: com.hd.nepm.vo
 * Description：
 *
 * @Author msy
 * @Create 2026/6/24 10:45
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData<T> {
    private int code;//200正常,1001错误,2001数据库异常
    private String message;//提示信息
    private T data;//响应数据
    //成功
    public static <T> ResponseData<T> success(T data){
        return new ResponseData<>(200,"success",data);
    }
    //失败
    public static <T> ResponseData<T> error(int code,String message){
        ResponseData<T> responseData=new ResponseData<>();
        responseData.setCode(code);
        responseData.setMessage(message);
        return responseData;
    }
}
