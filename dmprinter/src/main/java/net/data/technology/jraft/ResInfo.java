package net.data.technology.jraft;

public class ResInfo {
    private int exitStatus = Integer.MIN_VALUE;// 返回状态码 （在linux中可以通过 echo $? 可知每步执行令执行的状态码）
    private String outRes;// 标准正确输出流内容
    private String errRes;// 标准错误输出流内容

    public ResInfo() {}

    public ResInfo(int exitStatus, String outRes, String errRes) {
        this.exitStatus = exitStatus;
        this.outRes = outRes;
        this.errRes = errRes;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public String getOutRes() {
        return outRes;
    }

    public void setOutRes(String outRes) {
        this.outRes = outRes;
    }

    public String getErrRes() {
        return errRes;
    }

    public void setErrRes(String errRes) {
        this.errRes = errRes;
    }

    public boolean isSuccess() {
        return exitStatus == 0;
    }

    public void clear() {
        exitStatus = 0;
        outRes = null;
        errRes = null;
    }
}
