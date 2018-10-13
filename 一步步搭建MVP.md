####	MVP的认识
先来认识mvc

	m：model 模型（数据）
	v：view 视图（显示）
	c：control控制（逻辑）
介于Android中显示的载体像Activity、Fragment既包含控制又包含视图，所以基本的Android应用可以做到的是m-vc

相比mvc，那么mvp中使用p（persenter）表示业务，接显示和模型处理数据也视图的关系

#####	Model层
1、状态以及数据返回的定义

	public interface IModelCallback {
	    /**
	     * 开始
	     */
	    void onStart();
	
	    /**
	     * 处理成功
	     * @param data
	     */
	    void onSuccess(String data);
	
	    /**
	     * 处理失败
	     * @param msg
	     */
	    void onFail(String msg);
	
	    /**
	     * 处理出现异常
	     */
	    void onError();
	
	    /**
	     * 处理完毕
	     */
	    void onComplete();
	}
2、具体的数据加载

	/**
	 * 数据处理类
	 */
	public class MyModel1 {
	    private Handler m = new Handler();
	
	    public void loadData(final int par,final IModelCallback callback){
	        callback.onStart();
	        new Thread(){
	            @Override
	            public void run() {
	                try {
	                    Thread.sleep(1000);
	                    if(par < 0){
	                        m.post(new Runnable() {
	                            @Override
	                            public void run() {
	                                callback.onFail("参数非法错误");
	                            }
	                        });
	                    }else {
	                        final String data = "加载了" + par + "条数据";
	                        m.post(new Runnable() {
	                            @Override
	                            public void run() {
	                                callback.onSuccess(data);
	                            }
	                        });
	
	                    }
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                    m.post(new Runnable() {
	                        @Override
	                        public void run() {
	                            callback.onError();
	                        }
	                    });
	                }
	                m.post(new Runnable() {
	                    @Override
	                    public void run() {
	                        callback.onComplete();
	                    }
	                });
	            }
	        }.start();
	    }
	}
#####	view层
1、结合数据加载情况处理页面效果

	public interface IView {
	    /**
	     * 显示加载框
	     */
	    void showLoadingDialog();
	
	    /**
	     * 显示数据结果
	     * @param data
	     */
	    void showResult(String data);
	
	    /**
	     * 显示加载失败的提醒
	     * @param msg
	     */
	    void showFailMsg(String msg);
	
	    /**
	     * 显示加载异常的提醒
	     */
	    void showErrorMsg();
	
	    /**
	     * 隐藏加载框
	     */
	    void hideLoadingDialog();
	}
#####	Persenter层
承上启下

	public class MyPersenter1 {
	    private IView iView;
	
	    public MyPersenter1(IView iView) {
	        this.iView = iView;
	    }
	
	    public void getData(int par) {
	        MyModel1 model1 = new MyModel1();
	        model1.loadData(par, new IModelCallback() {
	            @Override
	            public void onStart() {
	                iView.showLoadingDialog();
	            }
	
	            @Override
	            public void onSuccess(String data) {
	                iView.showResult(data);
	            }
	
	            @Override
	            public void onFail(String msg) {
	                iView.showFailMsg(msg);
	            }
	
	            @Override
	            public void onError() {
	                iView.showErrorMsg();
	            }
	
	            @Override
	            public void onComplete() {
	                iView.hideLoadingDialog();
	            }
	        });
	    }
	}
界面的使用

	public class MainActivity extends AppCompatActivity implements View.OnClickListener,IView{

		...
		private MyPersenter1 persenter1;

		 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        tv = findViewById(R.id.tv_result);
	        tv.setOnClickListener(this);
	        persenter1 = new MyPersenter1(this);
	    }
	
	    int count = -1;
	    @Override
	    public void onClick(View view) {
	        persenter1.getData(count);
	        count++;
	    }
		

		@Override
	    public void showLoadingDialog() {
	        if(null == dialog){
	            dialog = ProgressDialog.show(this,"","数据加载中");
	        }else{
	            dialog.show();
	        }
	    }
	
	    @Override
	    public void showResult(String data) {
	        tv.setText(data);
	    }
	
	    @Override
	    public void showFailMsg(String msg) {
	        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
	    }
	
	    @Override
	    public void showErrorMsg() {
	        Toast.makeText(this,"加载异常",Toast.LENGTH_LONG).show();
	    }
	
	    @Override
	    public void hideLoadingDialog() {
	        dialog.dismiss();
	    }
	}
####	实用型的mvp
#####	1、model层中的数据统一封装

	public interface IModelCallback<T> {
	    /**
	     * 开始
	     */
	    void onStart();
	
	    /**
	     * 处理成功
	     * @param data
	     */
	    void onSuccess(T data);
	
	    /**
	     * 处理失败
	     * @param msg
	     */
	    void onFail(String msg);
	
	    /**
	     * 处理出现异常
	     */
	    void onError();
	
	    /**
	     * 处理完毕
	     */
	    void onComplete();
	}
针对数据加载流程统一封装

	public abstract class IModel<T> {
	    protected Map<String,String> par;
	
	    public IModel addPar(String key,String value){
	        if(par == null ){
	            par= new HashMap<>();
	        }
	        par.put(key,value);
	        return this;
	    }
	
	    /**
	     * 加载数据方法（管理线程，处理数据加载、转换）
	     * @param callback
	     */
	    public abstract void execute(IModelCallback<T> callback);
	}
#####	2、处理视图的共性部分

	public interface IView {
	    /**
	     * 显示加载框
	     */
	    void showLoadingDialog();
	
	    /**
	     * 显示加载失败的提醒
	     * @param msg
	     */
	    void showFailMsg(String msg);
	
	    /**
	     * 显示加载异常的提醒
	     */
	    void showErrorMsg();
	
	    /**
	     * 隐藏加载框
	     */
	    void hideLoadingDialog();
	}
#####	3、共性的业务层persenter
	public class IPersenter<T extends IView> {
	    protected T view;
	
	    public void registView(T view) {
	        this.view = view;
	    }
	
	    public void unRegistView() {
	        this.view = null;
	    }
	
	    public boolean isViewAvailable() {
	        return view != null;
	    }
	}
#####	结合界面(Activity、Fragment)处理业务和视图关系
	public abstract class BaseActivity<T extends IPersenter> extends FragmentActivity implements IView {
	    protected T persenter;
	    private ProgressDialog dialog;
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	
	        persenter = createPersenter();
	        persenter.registView(this);
	    }
	
	    //BaseActivity<T>   BaseActivity<T,M,K>
	    //LoginActivity<LoginPersonter> -->new LoginPersonter();
	    protected T createPersenter() {
	        try {
	            //父类中定义的Type
	            Type type = getClass().getGenericSuperclass();
	            if (type instanceof ParameterizedType) {
	                ParameterizedType parameterizedType = (ParameterizedType) type;
	                Type[] types = parameterizedType.getActualTypeArguments();
	                Class<T> cls = (Class<T>) types[0];
	                return cls.newInstance();
	            }
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	
	    @Override
	    protected void onDestroy() {
	        persenter.unRegistView();
	        super.onDestroy();
	    }
	
	    @Override
	    public void showLoadingDialog() {
	        if(null == dialog){
	            dialog = ProgressDialog.show(this,"","数据加载中");
	        }else{
	            dialog.show();
	        }
	    }
	
	    @Override
	    public void showFailMsg(String msg) {
	        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
	    }
	
	    @Override
	    public void showErrorMsg() {
	        Toast.makeText(this,"加载异常",Toast.LENGTH_LONG).show();
	    }
	
	    @Override
	    public void hideLoadingDialog() {
	        dialog.dismiss();
	    }
	}
#####	具体使用(个性化)
view:

	public  interface MyView1 extends IView {
	    void showData(String data);
	}
model:

	public class MyModel1 extends IModel<String>{
	    private Handler m = new Handler();
	
	    @Override
	    public void execute(final IModelCallback<String> callback) {
	        callback.onStart();
	        new Thread(){
	            @Override
	            public void run() {
	                try {
	                    Thread.sleep(1000);
	                    if(Integer.parseInt(par.get("num")) < 0){
	                        m.post(new Runnable() {
	                            @Override
	                            public void run() {
	                                callback.onFail("参数非法错误");
	                            }
	                        });
	                    }else {
	                        final String data = "加载了" + par.get("num") + "条数据";
	                        m.post(new Runnable() {
	                            @Override
	                            public void run() {
	                                callback.onSuccess(data);
	                            }
	                        });
	
	                    }
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                    m.post(new Runnable() {
	                        @Override
	                        public void run() {
	                            callback.onError();
	                        }
	                    });
	                }
	                m.post(new Runnable() {
	                    @Override
	                    public void run() {
	                        callback.onComplete();
	                    }
	                });
	            }
	        }.start();
	    }
	}
persenter:

	public class MyPersenter1 extends IPersenter<MyView1> {

	    public void getData(int par) {
	        MyModel1 model1 = new MyModel1();
	        model1.addPar("num", "" + par);
	        model1.execute(new IModelCallback<String>() {
	            @Override
	            public void onStart() {
	                if (isViewAvailable())
	                    view.showLoadingDialog();
	            }
	
	            @Override
	            public void onSuccess(String data) {
	                if (isViewAvailable())
	                    view.showData(data);
	            }
	
	            @Override
	            public void onFail(String msg) {
	                if (isViewAvailable())
	                    view.showFailMsg(msg);
	            }
	
	            @Override
	            public void onError() {
	                if (isViewAvailable())
	                    view.showErrorMsg();
	            }
	
	            @Override
	            public void onComplete() {
	                if (isViewAvailable())
	                    view.hideLoadingDialog();
	            }
	        });
	
	    }
	}
界面使用

	public class MainActivity extends BaseActivity<MyPersenter1> implements View.OnClickListener, MyView1 {

	    private TextView tv;
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        tv = findViewById(R.id.tv_result);
	        tv.setOnClickListener(this);
	    }
	
	    int count = -1;
	
	    @Override
	    public void onClick(View view) {
	        persenter.getData(count);
	        count++;
	    }
	
	    @Override
	    public void showData(String data) {
	        tv.setText(data);
	    }
	}
