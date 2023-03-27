package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    inner class DownLoadHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                DownLoadThread.STATE_START -> {
                    binding.downText.text = "还没开始下载呢"
                    binding.downState.text = "开始下载"
                }
                DownLoadThread.STATE_DOWNLOADING -> {
                    binding.downText.text = "下载了 ${msg.obj as Int} %"
                    binding.downState.text = "开始下载"
                }
                DownLoadThread.STATE_FINISH -> {
                    binding.downText.text = "下载完成了"
                    binding.downState.text = "下载完成"
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val downLoadThread = DownLoadThread()
        downLoadThread.start()
        downLoadThread.mUIHandler = DownLoadHandler()
        WatchDog.addMonitor(downLoadThread)
        WatchDog.start()
        binding.buttonStart.setOnClickListener {
            downLoadThread.startDownload()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}