package nc.bs.framework.core;

import nc.bs.framework.component.ServiceComponent;
import java.util.Comparator;
import java.util.Arrays;
import nc.bs.framework.exception.FrameworkRuntimeException;
import nc.bs.framework.exception.DuplicateException;
import nc.bs.framework.exception.ComponentNotFoundException;
import nc.bs.framework.exception.ComponentException;
import java.util.Iterator;
import org.granite.convert.ConverterManager;
import nc.bs.framework.aop.AspectManager;
import org.granite.convert.Converter;
import org.granite.convert.ClassConverter;
import nc.bs.framework.util.EsaConverterManager;
import nc.bs.framework.enhancer.InterceptorEnhancer;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.List;
import java.util.Map;
import nc.bs.logging.Log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractGenericContainer<T extends Meta> implements GenericContainer<T>
{
    protected HashMap<String, MetaList<T>> metaMaps;
    protected ArrayList<T> metas;
    private Log log;
    private ClassLoader loader;
    private String name;
    private EnhancerManager em;
    protected State state;
    protected Map<Class<?>, Object> extensions;
    protected List<LifeCycleListener> listeners;
    protected ReentrantReadWriteLock accessLock;
    
    protected AbstractGenericContainer()
    {
    }
    
    public AbstractGenericContainer(final String name)
    {
        this(name, null);
    }
    
    public AbstractGenericContainer(final String name, final ClassLoader loader)
    {
        this.extensions = new HashMap<Class<?>, Object>();
        this.metaMaps = new HashMap<String, MetaList<T>>();
        this.metas = new ArrayList<T>();
        this.loader = ((loader == null) ? AbstractGenericContainer.class.getClassLoader() : loader);
        this.name = name;
        this.log = Log.getInstance(this.getClass().getName() + "." + name);
        this.listeners = new LinkedList<LifeCycleListener>();
        this.em = (EnhancerManager) new SimpleEnhancerManager();
        final AspectManager am = (AspectManager) new AspectManagerImpl();
        this.em.addPostEnhancer((Enhancer) new InterceptorEnhancer());
        this.em.addPostEnhancer((Enhancer) new AspectEnhancer(am));
        this.accessLock = new ReentrantReadWriteLock();
        final ConverterManager cm = (ConverterManager) new EsaConverterManager();
        cm.registerConverter((Class) Class.class, (Converter) new ClassConverter(this.getClassLoader()));
        this.setExtension(am, AspectManager.class);
        this.setExtension(cm, ConverterManager.class);
        this.state = State.NOT_INIT;
    }
    
    public boolean contains(final String name, final int rank)
    {
        this.accessLock.readLock().lock();
        try
        {
            final MetaList<T> ml = this.metaMaps.get(name);
            return ml != null && ml.getForRank(rank) != null;
        }
        finally
        {
            this.accessLock.readLock().unlock();
        }
    }
    
    public T deregister(final String name) throws ComponentException
    {
        this.accessLock.writeLock().lock();
        try
        {
            final MetaList<T> ml = this.metaMaps.remove(name);
            if (ml != null)
            {
                for (final T meta : ml)
                {
                    final String[] arr$;
                    final String[] alias = arr$ = meta.getAlias();
                    for (final String s : arr$)
                    {
                        this.metaMaps.remove(s);
                    }
                    if (this.state == State.RUNING && meta != null && meta.isActive())
                    {
                        this.stopActive(meta);
                    }
                    this.metas.remove(meta);
                }
                return (T) ml.getDefault();
            }
            return null;
        }
        finally
        {
            this.accessLock.writeLock().unlock();
        }
    }
    
    public ClassLoader getClassLoader()
    {
        return this.loader;
    }
    
    public T getMeta(final String name, final int rank)
    {
        this.accessLock.readLock().lock();
        try
        {
            final MetaList<T> ml = this.metaMaps.get(name);
            final T meta = (T) ((ml == null) ? null : ml.getForRank(rank));
            if (meta == null)
            {
                throw new ComponentNotFoundException(this.getName(), name + "(" + rank + ")");
            }
            return meta;
        }
        finally
        {
            this.accessLock.readLock().unlock();
        }
    }
    
    public T getMeta(final String name) throws ComponentException
    {
        this.accessLock.readLock().lock();
        try
        {
            final MetaList<T> ml = this.metaMaps.get(name);
            final T meta = (T) ((ml == null) ? null : ml.getDefault());
            if (meta == null)
            {
                throw new ComponentNotFoundException(this.getName(), name);
            }
            return meta;
        }
        finally
        {
            this.accessLock.readLock().unlock();
        }
    }
    
    public EnhancerManager getEnhancerManager()
    {
        return this.em;
    }
    
    public Log getLogger()
    {
        return this.log;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public State getState()
    {
        return this.state;
    }
    
    public void register(final T meta) throws ComponentException
    {
        this.accessLock.writeLock().lock();
        try
        {
            if (this.isIllegalMeta(meta))
            {
                throw new DuplicateException(meta.getName());
            }
            this.register(meta.getName(), meta);
            final String[] arr$;
            final String[] strs = arr$ = meta.getAlias();
            for (final String s : arr$)
            {
                if (!s.equals(meta.getName()))
                {
                    this.register(s, meta);
                }
            }
            this.metas.add(meta);
            if (this.state == State.RUNING && meta.isActive())
            {
                this.startActive(meta);
            }
        }
        finally
        {
            this.accessLock.writeLock().unlock();
        }
    }
    
    public void setEnhancerManager(final EnhancerManager factory)
    {
        if (factory == null)
        {
            throw new IllegalArgumentException("EnhancerManager can't be null");
        }
        this.em = factory;
    }
    
    public synchronized void addLifecycleListener(final LifeCycleListener listener)
    {
        synchronized (this.listeners)
        {
            this.listeners.add(listener);
        }
        if (this.state == State.RUNING)
        {
            try
            {
                listener.afterStart((LifeCycle) this);
            }
            catch (final Throwable thr)
            {
                this.getLogger().error((Object) String.format("life cycle listener callback  %s error: %s ", "afterStart", listener), thr);
            }
        }
    }
    
    public void removeLifecycleListener(final LifeCycleListener listener)
    {
        synchronized (this.listeners)
        {
            this.listeners.remove(listener);
        }
    }
    
    private PrintStream pw;
    
    public void start() throws Exception
    {
        this.pw = System.out;
        if (this.state != State.INITED && this.state != State.STOPPED)
        {
            this.pw.println(String.format(" %s not  inited or is running", this.getName()));
            throw new FrameworkRuntimeException(String.format(" %s not  inited or is running", this.getName()));
        }
        this.pw.println(this.getName() + " start....");
        this.getLogger().debug((Object) (this.getName() + " start...."));
        this.state = State.STARTING;
        this.beforeInternalStart();
        this.internalStart();
        this.afterInternalStart();
        this.pw.println(this.getName() + " now is running....");
        this.getLogger().debug((Object) (this.getName() + " now is running...."));
        this.fireAfterStart();
        this.state = State.RUNING;
    }
    
    protected void fireAfterStart()
    {
        final LifeCycleListener[] ls = this.listeners.toArray(new LifeCycleListener[0]);
        if (ls.length == 0)
        {
            return;
        }
        for (final LifeCycleListener l : ls)
        {
            try
            {
                l.afterStart((LifeCycle) this);
            }
            catch (final Throwable thr)
            {
                this.getLogger().error((Object) ("afterStart callback error: " + l), thr);
            }
        }
    }
    
    protected void internalStart()
    {
        final T[] metas = (T[]) this.getMetas();
        Arrays.sort(metas, (Comparator<? super T>) new PriorityComparator(true));
        for (int i = 0; i < metas.length; ++i)
        {
            final ExtensionProcessor[] eps = metas[i].getExtensionProcessors();
            if (eps != null)
            {
                for (final ExtensionProcessor ep : eps)
                {
                    try
                    {
                        ep.processAtStart(metas[i].getContainer(), (Meta) metas[i]);
                    }
                    catch (final Throwable e)
                    {
                        this.getLogger()
                            .error((Object) String.format("extension process for component: %s at container: %s error when start",
                                metas[i].getName(), this.getName()), e);
                    }
                }
            }
        }
        for (int i = 0; i < metas.length; ++i)
        {
            if (metas[i].isActive())
            {
                try
                {
                    this.startActive(metas[i]);
                }
                catch (final Throwable e2)
                {
                    this.getLogger().error(
                        (Object) String.format("service component: %s at container: %s start error", metas[i].getName(), this.getName()),
                        e2);
                }
            }
        }
    }
    
    protected void beforeInternalStart()
    {
    }
    
    protected void afterInternalStart()
    {
    }
    
    protected void beforeInternalStop()
    {
    }
    
    protected void afterInternalStop()
    {
    }
    
    public void stop() throws Exception
    {
        if (this.state != State.RUNING && this.state != State.STARTING)
        {
            this.getLogger().debug((Object) String.format("conteiner: %s alread stopped", this.getName()));
        }
        this.getLogger().debug((Object) String.format("conteiner: %s begin stop...", this.getName()));
        this.fireBeforeStop();
        this.state = State.STOPPING;
        this.beforeInternalStop();
        this.internalStop();
        this.afterInternalStop();
        this.metaMaps.clear();
        this.state = State.STOPPED;
        this.getLogger().debug((Object) String.format("conteiner: %s end stop", this.getName()));
    }
    
    protected void internalStop()
    {
        final T[] metas = (T[]) this.getMetas();
        Arrays.sort(metas, (Comparator<? super T>) new PriorityComparator(false));
        for (int i = 0; i < metas.length; ++i)
        {
            if (metas[i].isActive())
            {
                this.stopActive(metas[i]);
            }
        }
        for (int i = 0; i < metas.length; ++i)
        {
            if (this.isHost(metas[i]))
            {
                final ExtensionProcessor[] eps = metas[i].getExtensionProcessors();
                if (eps != null)
                {
                    for (final ExtensionProcessor ep : eps)
                    {
                        try
                        {
                            ep.processAtStop(metas[i].getContainer(), (Meta) metas[i]);
                        }
                        catch (final Throwable e)
                        {
                            this.getLogger()
                                .error((Object) String.format("extension process for component: %s at container: %s error when stop",
                                    metas[i].getName(), this.getName()), e);
                        }
                    }
                }
            }
        }
    }
    
    protected void fireBeforeStop()
    {
        final LifeCycleListener[] arr$;
        final LifeCycleListener[] ls = arr$ = this.listeners.toArray(new LifeCycleListener[0]);
        for (final LifeCycleListener l : arr$)
        {
            try
            {
                l.beforeStop((LifeCycle) this);
            }
            catch (final Throwable thr)
            {
                this.getLogger().error((Object) String.format("life cycle listener callback  %s error: %s ", "beforeStop", l), thr);
            }
        }
    }
    
    protected boolean isIllegalMeta(final T meta)
    {
        final String name = meta.getName();
        if (this.contains(name, meta.getRank()))
        {
            return true;
        }
        final String[] alias = meta.getAlias();
        for (int i = 0; alias != null && i < alias.length; ++i)
        {
            if (this.contains(alias[i], meta.getRank()))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isHost(final T meta)
    {
        return true;
    }
    
    protected void processActiveAtStart(final Object obj)
    {
    }
    
    protected void processActiveAtStop(final Object obj)
    {
    }
    
    protected void startActive(final T meta)
    {
        if (this.isHost(meta))
        {
            final Object retObject = meta.getInstantiator().instantiate(this.getContext(), meta.getName(), (Object[]) null);
            if (retObject instanceof ServiceComponent)
            {
                try
                {
                    this.getLogger().debug(
                        (Object) String.format("service component: %s at container: %s begin start...", meta.getName(), this.getName()));
                    if (!((ServiceComponent) retObject).isStarted())
                    {
                        ((ServiceComponent) retObject).start();
                    }
                    this.getLogger()
                        .debug((Object) String.format("service component: %s at container: %s end start", meta.getName(), this.getName()));
                    this.processActiveAtStart(retObject);
                }
                catch (final Throwable e)
                {
                    this.getLogger().error(
                        (Object) String.format("service component: %s at container: %s start error", meta.getName(), this.getName()), e);
                }
            }
        }
    }
    
    protected void stopActive(final T meta)
    {
        if (this.isHost(meta))
        {
            try
            {
                final Object retObject = meta.getInstantiator().instantiate(this.getContext(), meta.getName(), (Object[]) null);
                if (retObject instanceof ServiceComponent)
                {
                    synchronized (retObject)
                    {
                        this.processActiveAtStop(retObject);
                        this.getLogger().debug(
                            (Object) String.format("service component: %s at container: %s begin stop...", meta.getName(), this.getName()));
                        if (((ServiceComponent) retObject).isStarted())
                        {
                            ((ServiceComponent) retObject).stop();
                        }
                        this.getLogger().debug(
                            (Object) String.format("service component: %s at container: %s end stop", meta.getName(), this.getName()));
                    }
                }
            }
            catch (final Throwable exp)
            {
                this.getLogger().error(
                    (Object) String.format("service component: %s at container: %s stop error", meta.getName(), this.getName()), exp);
            }
        }
    }
    
    @Override
    public String toString()
    {
        return this.getName();
    }
    
    @Override
    public int hashCode()
    {
        return (this.getName() != null) ? this.getName().hashCode() : super.hashCode();
    }
    
    public <P> P getExtension(final Class<P> extensionType)
    {
        final Object obj = this.extensions.get(extensionType);
        if (null != obj)
        {
            return extensionType.cast(obj);
        }
        return null;
    }
    
    public <P> void setExtension(final P extension, final Class<P> extensionType)
    {
        this.extensions.put(extensionType, extension);
    }
    
    private boolean register(final String name, final T meta)
    {
        MetaList<T> ml = this.metaMaps.get(name);
        if (ml == null)
        {
            ml = (MetaList<T>) new MetaList();
            this.metaMaps.put(name, ml);
        }
        return ml.add((T) meta);
    }
    
    public boolean contains(final String name)
    {
        final MetaList<T> ml = this.metaMaps.get(name);
        return ml != null && ml.size() > 0;
    }
}
